/*
 * Gera os ícones do app a partir do mascote em pixel art (src/game/pixel/art/brand.ts):
 *   public/favicon.svg, public/icons/pwa-192.png, pwa-512.png, pwa-maskable-512.png e apple-touch-icon.png.
 *
 * Rode com:  node scripts/generate-icons.ts   (Node 24 executa TypeScript direto)
 * Os PNGs são montados aqui mesmo (zlib do Node), sem dependências.
 */
import { writeFileSync } from 'node:fs'
import { deflateSync } from 'node:zlib'

import { BRAND_BACKGROUND, MASCOT } from '../src/game/pixel/art/brand.ts'

const INK = '#1a1c2c'
const OUT = new URL('../public/', import.meta.url)

type Rgba = [number, number, number, number]

function hex(color: string): Rgba {
  const value = Number.parseInt(color.slice(1), 16)
  return [(value >> 16) & 255, (value >> 8) & 255, value & 255, 255]
}

/**
 * Desenha o ícone numa grade de "pixels da arte": fundo amarelo, contorno de tinta (só nos ícones normais) e o
 * mascote encostado embaixo, centralizado. `grid` é o tamanho do ícone em pixels da arte.
 */
function artGrid(grid: number, options: { border: boolean; inset: number; sparkles?: [number, number][] }): string[][] {
  const cells = Array.from({ length: grid }, () => Array<string>(grid).fill(BRAND_BACKGROUND))
  if (options.border) {
    for (let i = 0; i < grid; i++) {
      cells[0][i] = cells[grid - 1][i] = cells[i][0] = cells[i][grid - 1] = INK
    }
  }
  const height = MASCOT.rows.length
  const width = MASCOT.rows[0].length
  const left = Math.floor((grid - width) / 2)
  const top = grid - height - options.inset
  MASCOT.rows.forEach((row, y) => {
    ;[...row].forEach((key, x) => {
      const color = MASCOT.palette[key]
      if (key !== '.' && color) cells[top + y][left + x] = color
    })
  })
  // Dois brilhos no alto, como num item que acabou de ser ganho
  for (const [cx, cy] of options.sparkles ?? []) {
    for (const [dx, dy] of [[0, 0], [1, 0], [-1, 0], [0, 1], [0, -1]]) cells[cy + dy][cx + dx] = '#ffffff'
  }
  return cells
}

const CRC_TABLE = Array.from({ length: 256 }, (_, n) => {
  let c = n
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1
  return c >>> 0
})

function crc32(bytes: Buffer): number {
  let crc = 0xffffffff
  for (const byte of bytes) crc = CRC_TABLE[(crc ^ byte) & 255] ^ (crc >>> 8)
  return (crc ^ 0xffffffff) >>> 0
}

function chunk(type: string, data: Buffer): Buffer {
  const length = Buffer.alloc(4)
  length.writeUInt32BE(data.length)
  const body = Buffer.concat([Buffer.from(type, 'ascii'), data])
  const crc = Buffer.alloc(4)
  crc.writeUInt32BE(crc32(body))
  return Buffer.concat([length, body, crc])
}

/** PNG RGBA de `size` px, ampliando cada pixel da arte em blocos nítidos. */
function png(cells: string[][], size: number): Buffer {
  const grid = cells.length
  const scale = size / grid
  const raw = Buffer.alloc((size * 4 + 1) * size)
  for (let y = 0; y < size; y++) {
    const rowStart = y * (size * 4 + 1)
    raw[rowStart] = 0 // filtro "nenhum"
    for (let x = 0; x < size; x++) {
      const [r, g, b, a] = hex(cells[Math.floor(y / scale)][Math.floor(x / scale)])
      raw.set([r, g, b, a], rowStart + 1 + x * 4)
    }
  }
  const header = Buffer.alloc(13)
  header.writeUInt32BE(size, 0)
  header.writeUInt32BE(size, 4)
  header.set([8, 6, 0, 0, 0], 8) // 8 bits, RGBA
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', header),
    chunk('IDAT', deflateSync(raw, { level: 9 })),
    chunk('IEND', Buffer.alloc(0)),
  ])
}

/** SVG com uma faixa por sequência de pixels iguais: o favicon fica nítido em qualquer tamanho. */
function svg(cells: string[][]): string {
  const grid = cells.length
  const rects: string[] = []
  cells.forEach((row, y) => {
    let x = 0
    while (x < row.length) {
      let end = x + 1
      while (end < row.length && row[end] === row[x]) end++
      rects.push(`<rect x="${x}" y="${y}" width="${end - x}" height="1" fill="${row[x]}"/>`)
      x = end
    }
  })
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${grid} ${grid}" shape-rendering="crispEdges">${rects.join('')}</svg>\n`
}

// Ícone normal: o mascote encostado embaixo, com contorno, numa grade de 20 (blocos de 9-10 px em 192 e
// 25-26 px em 512: o arredondamento por bloco mantém os pixels retos)
const icon = artGrid(20, { border: true, inset: 0, sparkles: [[4, 3], [16, 4]] })
// Maskable: sem contorno e com o mascote dentro da zona segura (o círculo de 80% do centro)
const maskable = artGrid(32, { border: false, inset: 8, sparkles: [[9, 7], [23, 8]] })
// Favicon: grade justa de 18 para ficar legível em 16-32 px
const favicon = artGrid(18, { border: true, inset: 0 })

writeFileSync(new URL('favicon.svg', OUT), svg(favicon))
writeFileSync(new URL('icons/pwa-192.png', OUT), png(icon, 192))
writeFileSync(new URL('icons/pwa-512.png', OUT), png(icon, 512))
writeFileSync(new URL('icons/pwa-maskable-512.png', OUT), png(maskable, 512))
writeFileSync(new URL('icons/apple-touch-icon.png', OUT), png(artGrid(20, { border: false, inset: 0, sparkles: [[4, 3], [16, 4]] }), 180))
console.log('Ícones gerados em public/')
