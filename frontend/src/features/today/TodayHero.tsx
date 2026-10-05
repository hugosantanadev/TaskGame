import type { CharacterState, Period, TitleCode } from '../../api/types'
import { BOOK, CLOUD, LAPTOP, MOON, NIGHT_CLOUD, SUN, SUNSET_SUN, TWINKLE } from '../../game/pixel/art/scenery'
import { Avatar } from '../../game/pixel/Avatar'
import { PixelIcon, PixelSprite } from '../../game/pixel/PixelSprite'
import { isMasterTitle, titleLabel } from '../../lib/titles'
import styles from './TodayHero.module.css'

type Props = {
  period: Period
  greeting: string
  /** Data já escrita: número do dia, dia da semana e "mês de ano". */
  day: { day: string; weekday: string; month: string; year: string }
  wearing: readonly string[]
  state: CharacterState
  title: TitleCode | null
}

const TWINKLES = [
  { left: '8%', top: '18%', delay: '0s' },
  { left: '32%', top: '10%', delay: '0.8s' },
  { left: '55%', top: '26%', delay: '1.6s' },
  { left: '44%', top: '48%', delay: '0.4s' },
]

/**
 * A cena do dia: o céu muda com o período (manhã, tarde, pôr do sol, noite), as nuvens passam devagar e o
 * personagem fica no gramado, com o objeto da tarefa em andamento (livro, notebook) ou dormindo.
 */
export function TodayHero({ period, greeting, day, wearing, state, title }: Props) {
  const night = period === 'NIGHT'
  const sky = night ? MOON : period === 'SUNSET' ? SUNSET_SUN : SUN
  const cloud = night ? NIGHT_CLOUD : CLOUD
  const prop = state === 'AT_COMPUTER' ? LAPTOP : state === 'STUDYING' || state === 'READING' ? BOOK : null

  return (
    <header className={styles.hero} data-period={period}>
      <div className={styles.decor} aria-hidden="true">
        <PixelSprite sprite={sky} scale={4} className={styles.sun} />
        <PixelSprite sprite={cloud} scale={4} className={styles.cloudA} />
        <PixelSprite sprite={cloud} scale={3} className={styles.cloudB} />
        {night &&
          TWINKLES.map((star) => (
            <PixelSprite
              key={star.left}
              sprite={TWINKLE}
              scale={3}
              className={styles.twinkle}
              style={{ left: star.left, top: star.top, animationDelay: star.delay }}
            />
          ))}
      </div>

      <div className={styles.copy}>
        <p className={styles.greeting}>{greeting}</p>
        {title && (
          <p className={styles.title} data-master={isMasterTitle(title) || undefined}>
            <PixelIcon name="star" />
            {titleLabel(title)}
          </p>
        )}
        <h1 className={styles.date}>
          <span className="visually-hidden">
            Hoje, {day.weekday}, {day.day} de {day.month} de {day.year}
          </span>
          <span className={styles.day} aria-hidden="true">
            {day.day}
          </span>
          <span className={styles.dateText} aria-hidden="true">
            <span className={styles.weekday}>{day.weekday}</span>
            <span className={styles.month}>
              {day.month} de {day.year}
            </span>
          </span>
        </h1>
      </div>

      <div className={styles.player} aria-hidden="true">
        {state === 'SLEEPING' && (
          <span className={styles.zzz}>
            <span>z</span>
            <span>z</span>
          </span>
        )}
        <Avatar wearing={wearing} sleeping={state === 'SLEEPING'} scale={5} className={styles.avatar} />
        {prop && <PixelSprite sprite={prop} scale={3} className={styles.prop} />}
      </div>
      <div className={styles.ground} aria-hidden="true" />
    </header>
  )
}
