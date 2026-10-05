/**
 * Motor de pixel art do GasmTask: um sprite é uma grade de letras, cada letra uma cor da paleta e "." um pixel
 * vazio. Desenhar assim deixa a arte no código, versionada e revisável como texto, sem arquivos de imagem.
 */

export type Palette = Record<string, string>

export type Sprite = {
  rows: readonly string[]
  palette: Palette
}

/** Uma faixa horizontal de pixels da mesma cor: vira um único retângulo no SVG. */
export type Run = { x: number; y: number; width: number; color: string }

export function spriteSize(sprite: Pick<Sprite, 'rows'>): { width: number; height: number } {
  return { width: Math.max(0, ...sprite.rows.map((row) => row.length)), height: sprite.rows.length }
}

/**
 * Junta pixels vizinhos da mesma cor em faixas, linha a linha, para o SVG ter poucas formas. Letras sem cor na
 * paleta são ignoradas (ficam vazias), como o ".".
 */
export function toRuns(sprite: Sprite): Run[] {
  const runs: Run[] = []
  sprite.rows.forEach((row, y) => {
    let x = 0
    while (x < row.length) {
      const key = row[x]
      const color = key === '.' ? undefined : sprite.palette[key]
      let end = x + 1
      while (end < row.length && row[end] === key) end++
      if (color) runs.push({ x, y, width: end - x, color })
      x = end
    }
  })
  return runs
}

/**
 * Empilha camadas do mesmo tamanho: cada pixel não vazio de uma camada de cima cobre o de baixo. É assim que a
 * roupa vai por cima do corpo e a capa por trás dele.
 */
export function stack(layers: readonly Sprite[]): Sprite {
  const { width, height } = layers.reduce(
    (size, layer) => {
      const next = spriteSize(layer)
      return { width: Math.max(size.width, next.width), height: Math.max(size.height, next.height) }
    },
    { width: 0, height: 0 },
  )
  const palette: Palette = {}
  const grid: string[][] = Array.from({ length: height }, () => Array<string>(width).fill('.'))
  layers.forEach((layer, index) => {
    // Cada camada ganha letras próprias para que duas paletas com a mesma letra não se misturem
    const remap = new Map<string, string>()
    layer.rows.forEach((row, y) => {
      for (let x = 0; x < row.length; x++) {
        const key = row[x]
        const color = key === '.' ? undefined : layer.palette[key]
        if (!color) continue
        let mapped = remap.get(key)
        if (!mapped) {
          mapped = String.fromCodePoint(0x100 + index * 64 + remap.size)
          remap.set(key, mapped)
          palette[mapped] = color
        }
        grid[y][x] = mapped
      }
    })
  })
  return { rows: grid.map((row) => row.join('')), palette }
}

/** Troca cores da paleta mantendo o desenho: um mesmo baú vira madeira, prata, ouro ou lendário. */
export function recolor(sprite: Sprite, colors: Palette): Sprite {
  return { rows: sprite.rows, palette: { ...sprite.palette, ...colors } }
}

/** Espelha na horizontal (personagem olhando para o outro lado). */
export function mirror(sprite: Sprite): Sprite {
  const { width } = spriteSize(sprite)
  return { rows: sprite.rows.map((row) => [...row.padEnd(width, '.')].reverse().join('')), palette: sprite.palette }
}
