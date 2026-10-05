import type { Chest, WeekSummary } from '../../api/types'
import { asApiError } from '../../api/errors'
import { Button } from '../../components/Button'
import { chestSprite } from '../../game/pixel/art/scenery'
import { PixelSprite } from '../../game/pixel/PixelSprite'
import { CHEST_LABEL, chestLook, chestTierFor, nextChest } from '../../lib/chest'
import { plural } from '../../lib/days'
import styles from './ChestCard.module.css'
import { useOpenChest } from './chestApi'

/** O baú da semana passada, esperando ser aberto. Ele balança até a pessoa tocar. */
export function ChestCard({ chest }: { chest: Chest }) {
  const open = useOpenChest()
  const contents = [plural(chest.coins, 'moeda', 'moedas'), `${chest.xp} XP`]
  if (chest.hasItem) contents.push('um item surpresa')

  return (
    <section className={styles.card} data-tier={chest.tier} aria-labelledby="chest-title">
      <PixelSprite sprite={chestSprite(chestLook(chest.tier))} scale={5} className={styles.chest} />
      <div className={styles.text}>
        <p className={styles.kicker}>Recompensa da semana</p>
        <h2 id="chest-title" className={styles.title}>
          {CHEST_LABEL[chest.tier]}
        </h2>
        <p className={styles.detail}>
          {plural(chest.fulfilledDays, 'dia cumprido', 'dias cumpridos')} na semana passada. Dentro: {contents.join(', ')}.
        </p>
        {open.error && (
          <p className={styles.error} role="alert">
            {asApiError(open.error).message}
          </p>
        )}
        <Button onClick={() => open.mutate(chest)} disabled={open.isPending} className={styles.button}>
          {open.isPending ? 'Abrindo…' : 'Abrir o baú'}
        </Button>
      </div>
    </section>
  )
}

/** Quanto falta para o baú desta semana melhorar: os 7 dias como casas e o próximo baú. */
export function ChestProgress({ summary }: { summary: WeekSummary }) {
  const days = summary.fulfilledDays
  const tier = chestTierFor(days)
  const next = nextChest(days)
  const remaining = summary.days.filter((day) => day.status === 'PENDING' || day.status === 'UPCOMING').length

  let message: string
  if (!next) message = 'Semana perfeita: o baú lendário está garantido.'
  else if (next.missing > remaining) message = tier === 'NONE' ? 'Nesta semana não dá mais baú.' : `Seu baú desta semana: ${CHEST_LABEL[tier].toLowerCase()}.`
  else message = `Cumpra mais ${plural(next.missing, 'dia', 'dias')} para o ${CHEST_LABEL[next.tier].toLowerCase()}.`

  return (
    <section className={styles.progress} aria-labelledby="chest-progress-title">
      <PixelSprite sprite={chestSprite(chestLook(next?.tier ?? 'LEGENDARY'))} scale={3} className={styles.mini} />
      <div className={styles.text}>
        <h2 id="chest-progress-title" className={styles.progressTitle}>
          Baú da semana
        </h2>
        <p className={styles.detail}>{message}</p>
        <ol className={styles.pips} aria-label={`${days} de 7 dias cumpridos`}>
          {summary.days.map((day) => (
            <li key={day.date} data-status={day.status} />
          ))}
        </ol>
      </div>
    </section>
  )
}
