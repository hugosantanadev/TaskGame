import { useMemo, type CSSProperties, type ReactNode } from 'react'

import { ICONS, type IconName } from './art/scenery'
import { spriteSize, toRuns, type Sprite } from './sprite'

type Props = {
  sprite: Sprite
  /** Tamanho de cada pixel da arte, em px de tela. O CSS pode sobrescrever largura e altura. */
  scale?: number
  className?: string
  style?: CSSProperties
  /** Sem rótulo o sprite é decorativo e fica fora da leitura de tela. */
  label?: string
}

function Runs({ sprite }: { sprite: Sprite }) {
  const runs = useMemo(() => toRuns(sprite), [sprite])
  return runs.map((run) => (
    <rect key={`${run.x}-${run.y}`} x={run.x} y={run.y} width={run.width} height={1} fill={run.color} />
  ))
}

/** Desenha um sprite como SVG de retângulos nítidos: escala sem borrar e não precisa de arquivo de imagem. */
export function PixelSprite({ sprite, scale = 4, className, style, label }: Props) {
  const { width, height } = spriteSize(sprite)
  return (
    <svg
      viewBox={`0 0 ${width} ${height}`}
      width={width * scale}
      height={height * scale}
      shapeRendering="crispEdges"
      className={className}
      style={style}
      role={label ? 'img' : undefined}
      aria-label={label}
      aria-hidden={label ? undefined : true}
      focusable="false"
    >
      <Runs sprite={sprite} />
    </svg>
  )
}

/** Ícone de pixel do tamanho da letra (1.25em), para ficar ao lado de números no placar. */
export function PixelIcon({ name, className }: { name: IconName; className?: string }) {
  const sprite = ICONS[name]
  const { width, height } = spriteSize(sprite)
  return (
    <svg
      viewBox={`0 0 ${width} ${height}`}
      width="1.25em"
      height="1.25em"
      shapeRendering="crispEdges"
      className={className}
      aria-hidden="true"
      focusable="false"
      style={{ flex: 'none' }}
    >
      <Runs sprite={sprite} />
    </svg>
  )
}

type SceneProps = {
  /** Tamanho da cena em pixels da arte. */
  width: number
  height: number
  className?: string
  label?: string
  children: ReactNode
}

/**
 * Cena de pixel art: um SVG só, onde cada sprite entra com <Placed>. A cena estica até a largura disponível
 * mantendo a proporção, sem borrar.
 */
export function PixelScene({ width, height, className, label, children }: SceneProps) {
  return (
    <svg
      viewBox={`0 0 ${width} ${height}`}
      shapeRendering="crispEdges"
      preserveAspectRatio="xMidYMax meet"
      className={className}
      role={label ? 'img' : undefined}
      aria-label={label}
      aria-hidden={label ? undefined : true}
      focusable="false"
    >
      {children}
    </svg>
  )
}

/**
 * Um sprite dentro da cena, com o canto de cima à esquerda em (x, y). A classe vai num grupo interno: uma
 * animação de CSS (transform) mexe no sprite sem apagar a posição dele.
 */
export function Placed({ sprite, x, y, className }: { sprite: Sprite; x: number; y: number; className?: string }) {
  return (
    <g transform={`translate(${x} ${y})`}>
      <g className={className}>
        <Runs sprite={sprite} />
      </g>
    </g>
  )
}
