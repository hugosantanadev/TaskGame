import { useEffect, useId, useRef, useState } from 'react'

import type { OpenedChest, RankRef, StoreItem, TitleCode } from '../api/types'
import { ItemSticker } from '../features/store/ItemSticker'
import { useCharacter, useChooseTitle } from '../features/store/storeApi'
import { chestSprite } from '../game/pixel/art/scenery'
import { Avatar } from '../game/pixel/Avatar'
import { wornCodes } from '../game/pixel/items'
import { PixelIcon, PixelSprite } from '../game/pixel/PixelSprite'
import { momentsOf, type Moment } from '../game/moments'
import { onReward } from '../game/rewardFeedback'
import { CHEST_LABEL, chestLook } from '../lib/chest'
import { plural } from '../lib/days'
import { rankLabel } from '../lib/rank'
import { isMasterTitle, titleLabel, titleSentence } from '../lib/titles'
import { Button } from './Button'
import styles from './Celebration.module.css'
import { RankBadge } from './RankBadge'

/** Confete de pixel: posições fixas para não pular a cada renderização. */
const CONFETTI = Array.from({ length: 18 }, (_, index) => ({
  left: `${(index * 37) % 100}%`,
  delay: `${(index % 6) * 0.18}s`,
  duration: `${1.6 + (index % 4) * 0.35}s`,
  color: ['var(--coin)', 'var(--pen)', 'var(--done-strong)', 'var(--streak)', 'var(--cat-reading)', 'var(--xp)'][index % 6],
}))

/**
 * Comemoração em tela cheia: título novo ("Você é Mestre nos estudos"), subida de elo e baú aberto. Os
 * momentos entram numa fila e aparecem um por vez. Usa o <dialog> nativo (foco preso, Esc fecha).
 */
export function Celebration() {
  const [queue, setQueue] = useState<Moment[]>([])
  const ref = useRef<HTMLDialogElement>(null)
  const titleId = useId()
  const current = queue[0]

  useEffect(() => onReward((event) => setQueue((items) => [...items, ...momentsOf(event)])), [])

  useEffect(() => {
    const dialog = ref.current
    if (!dialog) return
    if (current && !dialog.open) dialog.showModal()
    if (!current && dialog.open) dialog.close()
  }, [current])

  const next = () => setQueue((items) => items.slice(1))

  return (
    <dialog ref={ref} className={styles.dialog} aria-labelledby={titleId} onClose={next}>
      {current && (
        <div className={styles.card} key={queue.length}>
          <div className={styles.confetti} aria-hidden="true">
            {CONFETTI.map((piece, index) => (
              <span
                key={index}
                style={{ left: piece.left, animationDelay: piece.delay, animationDuration: piece.duration, background: piece.color }}
              />
            ))}
          </div>
          {current.kind === 'title' && <TitleMoment code={current.code} titleId={titleId} onDone={next} />}
          {current.kind === 'rank' && <RankMoment rank={current.rank} items={current.items} titleId={titleId} onDone={next} />}
          {current.kind === 'chest' && <ChestMoment result={current.result} titleId={titleId} onDone={next} />}
        </div>
      )}
    </dialog>
  )
}

type MomentProps = { titleId: string; onDone: () => void }

function TitleMoment({ code, titleId, onDone }: MomentProps & { code: TitleCode }) {
  const character = useCharacter()
  const choose = useChooseTitle()
  const master = isMasterTitle(code)
  return (
    <>
      <p className={styles.kicker}>{master ? 'Título máximo!' : 'Título novo!'}</p>
      <div className={styles.stage}>
        <Avatar wearing={wornCodes(character.data)} scale={6} className={styles.hop} />
      </div>
      <h2 id={titleId} className={styles.headline}>
        {titleSentence(code)}
      </h2>
      <p className={styles.banner} data-master={master || undefined}>
        <PixelIcon name="star" /> {titleLabel(code)}
      </p>
      <p className={styles.note}>Seu título aparece no topo do dia e no ranking.</p>
      <div className={styles.actions}>
        <Button disabled={choose.isPending} onClick={() => choose.mutate(code, { onSuccess: onDone })}>
          Usar este título
        </Button>
        <Button variant="quiet" onClick={onDone}>
          Agora não
        </Button>
      </div>
    </>
  )
}

function RankMoment({ rank, items, titleId, onDone }: MomentProps & { rank: RankRef; items: StoreItem[] }) {
  return (
    <>
      <p className={styles.kicker}>Ranqueada</p>
      <div className={styles.stage}>
        <span className={styles.hop}>
          <RankBadge rank={rank} size="l" showLabel={false} />
        </span>
      </div>
      <h2 id={titleId} className={styles.headline}>
        Subiu para {rankLabel(rank)}!
      </h2>
      {items.length > 0 && (
        <ul className={styles.items}>
          {items.map((item) => (
            <li key={item.code}>
              <ItemSticker item={item} />
              <span>
                <strong>{item.name}</strong> foi para a sua coleção
              </span>
            </li>
          ))}
        </ul>
      )}
      <div className={styles.actions}>
        <Button onClick={onDone}>Continuar</Button>
      </div>
    </>
  )
}

function ChestMoment({ result, titleId, onDone }: MomentProps & { result: OpenedChest }) {
  return (
    <>
      <p className={styles.kicker}>{CHEST_LABEL[result.chest.tier]}</p>
      <div className={styles.stage}>
        <span className={styles.burst} aria-hidden="true" />
        <PixelSprite sprite={chestSprite(chestLook(result.chest.tier), true)} scale={7} className={styles.hop} />
      </div>
      <h2 id={titleId} className={styles.headline}>
        Baú aberto!
      </h2>
      <ul className={styles.loot}>
        <li>
          <PixelIcon name="coin" /> +{plural(result.coins, 'moeda', 'moedas')}
        </li>
        <li>
          <PixelIcon name="gem" /> +{result.xp.gained} XP
        </li>
      </ul>
      {result.item && (
        <ul className={styles.items}>
          <li>
            <ItemSticker item={result.item} />
            <span>
              <strong>{result.item.name}</strong> foi para a sua coleção
            </span>
          </li>
        </ul>
      )}
      {result.xp.promoted && <p className={styles.note}>E você subiu para {rankLabel(result.xp.status)}!</p>}
      <div className={styles.actions}>
        <Button onClick={onDone}>Pegar tudo</Button>
      </div>
    </>
  )
}
