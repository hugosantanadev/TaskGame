import type { EquipmentTrack } from '../../api/types'
import { EQUIPMENT_ART } from './art/equipment'
import { FURNITURE } from './art/furniture'
import { spriteSize, type Sprite } from './sprite'

/*
 * O quarto em 128 × 72 pixels da arte: parede até a linha 47, piso a partir da 48. Cada trilha de melhoria tem
 * um canto fixo (cama à esquerda, mesa sob a janela, estante à direita, treino na frente) e a decoração ocupa o
 * que sobra. O que está mais embaixo na tela é desenhado depois, por cima: a cadeira fica na frente da mesa.
 */
export const ROOM_WIDTH = 128
export const ROOM_HEIGHT = 72
export const FLOOR_Y = 48
const BACK = 50 // linha em que os móveis encostados na parede tocam o chão

export type Layer = 'wall' | 'flat' | 'object'

export type Placement = { key: string; sprite: Sprite; x: number; y: number; layer: Layer }

type Anchor = { x: number; bottom: number; layer?: Layer }

const TRACK_SPOT: Record<EquipmentTrack, Anchor | ((tier: number) => Anchor)> = {
  BED: { x: 2, bottom: BACK },
  PEACE: { x: 33, bottom: BACK },
  DESK: { x: 54, bottom: BACK },
  ORGANIZER: { x: 84, bottom: BACK },
  BOOKSHELF: (tier) =>
    tier === 1 ? { x: 106, bottom: 29, layer: 'wall' } : tier === 3 ? { x: 102, bottom: BACK } : { x: 110, bottom: BACK },
  GYM: { x: 4, bottom: 69 },
  COMPUTER: { x: 58, bottom: BACK }, // no chão; com mesa, vai para cima dela
}

/** Decoração: canto de cada item. Luminária, caneca e aquário procuram uma mesa ou uma cômoda antes. */
const DECOR_SPOT: Record<string, Anchor> = {
  poster_space: { x: 8, bottom: 24, layer: 'wall' },
  neon_sign: { x: 86, bottom: 13, layer: 'wall' },
  rug_round: { x: 44, bottom: 68, layer: 'flat' },
  chair_gamer: { x: 70, bottom: 62 },
  beanbag_purple: { x: 94, bottom: 68 },
  plant_small: { x: 112, bottom: 70 },
  aquarium_small: { x: 78, bottom: 70 },
  lamp_desk: { x: 79, bottom: 51 },
  mug_coffee: { x: 46, bottom: 51 },
}

export const WINDOW_SPOT = { x: 56, y: 6 }
export const PLAYER_SPOT = { x: 56, bottom: 66 }

function place(key: string, sprite: Sprite, anchor: Anchor): Placement {
  const { height } = spriteSize(sprite)
  return { key, sprite, x: anchor.x, y: anchor.bottom - height, layer: anchor.layer ?? 'object' }
}

/**
 * Tudo o que vai no quarto, na ordem de desenho: parede, chão (tapete) e, por fim, os objetos de trás para a
 * frente. O personagem entra como um objeto qualquer. Itens sem lugar conhecido ficam de fora.
 */
export function roomLayout(input: {
  equipment: Record<EquipmentTrack, number>
  items: readonly string[]
  player?: Sprite
}): Placement[] {
  const placements: Placement[] = []
  const decor = new Set(input.items.filter((code) => code in DECOR_SPOT && code in FURNITURE))

  // Melhorias (o computador espera a mesa)
  let desk: Placement | null = null
  let organizer: Placement | null = null
  for (const track of Object.keys(TRACK_SPOT) as EquipmentTrack[]) {
    if (track === 'COMPUTER') continue
    const tier = input.equipment[track] ?? 0
    const sprite = EQUIPMENT_ART[track][tier]
    if (!sprite) continue
    const spot = TRACK_SPOT[track]
    const placed = place(`equipment-${track}`, sprite, typeof spot === 'function' ? spot(tier) : spot)
    placements.push(placed)
    if (track === 'DESK') desk = placed
    if (track === 'ORGANIZER') organizer = placed
  }

  // Em cima da mesa, da esquerda para a direita: caneca, computador, luminária. O que não cabe vai para o chão.
  const computer = EQUIPMENT_ART.COMPUTER[input.equipment.COMPUTER ?? 0]
  if (desk && computer) {
    const deskWidth = spriteSize(desk.sprite).width
    const onDesk: { key: string; sprite: Sprite }[] = [{ key: 'equipment-COMPUTER', sprite: computer }]
    const tryAdd = (code: string, front: boolean) => {
      if (!decor.has(code)) return
      const candidate = { key: `decor-${code}`, sprite: FURNITURE[code] }
      const next = front ? [candidate, ...onDesk] : [...onDesk, candidate]
      if (next.reduce((sum, item) => sum + spriteSize(item.sprite).width, 0) <= deskWidth) {
        onDesk.splice(0, onDesk.length, ...next)
        decor.delete(code)
      }
    }
    tryAdd('lamp_desk', false)
    tryAdd('mug_coffee', true)
    const total = onDesk.reduce((sum, item) => sum + spriteSize(item.sprite).width, 0)
    let x = desk.x + Math.floor((deskWidth - total) / 2)
    for (const item of onDesk) {
      placements.push(place(item.key, item.sprite, { x, bottom: desk.y }))
      x += spriteSize(item.sprite).width
    }
  } else if (computer) {
    placements.push(place('equipment-COMPUTER', computer, TRACK_SPOT.COMPUTER as Anchor))
  }

  // O aquário fica em cima das caixas, da cômoda ou do guarda-roupa, se houver
  if (decor.has('aquarium_small') && organizer) {
    const aquarium = FURNITURE.aquarium_small
    const width = spriteSize(organizer.sprite).width
    placements.push(
      place('decor-aquarium_small', aquarium, {
        x: organizer.x + Math.floor((width - spriteSize(aquarium).width) / 2),
        bottom: organizer.y,
      }),
    )
    decor.delete('aquarium_small')
  }

  for (const code of decor) placements.push(place(`decor-${code}`, FURNITURE[code], DECOR_SPOT[code]))
  if (input.player) placements.push(place('player', input.player, PLAYER_SPOT))

  const order: Record<Layer, number> = { wall: 0, flat: 1, object: 2 }
  return placements
    .map((placement, index) => ({ placement, index }))
    .sort((a, b) => {
      const byLayer = order[a.placement.layer] - order[b.placement.layer]
      if (byLayer !== 0) return byLayer
      const bottom = (p: Placement) => p.y + spriteSize(p.sprite).height
      return bottom(a.placement) - bottom(b.placement) || a.index - b.index
    })
    .map(({ placement }) => placement)
}
