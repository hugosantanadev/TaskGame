import { useMemo } from 'react'
import { Link } from 'react-router'

import { useAuth } from '../../auth/context'
import { ButtonLink } from '../../components/Button'
import { PageTitle } from '../../components/PageTitle'
import { Wordmark } from '../../components/Wordmark'
import { dressCharacter } from '../../game/pixel/art/character'
import { CLOUD, ICONS, SUN, chestSprite, type IconName } from '../../game/pixel/art/scenery'
import { PixelIcon, PixelScene, Placed } from '../../game/pixel/PixelSprite'
import { RoomScene } from '../profile/RoomScene'
import styles from './LandingPage.module.css'

const FEATURES: { icon: IconName; title: string; text: string }[] = [
  {
    icon: 'flame',
    title: 'Sequência de dias',
    text: 'Cumpra as obrigatórias do dia e veja o fogo crescer. Um protetor salva a sequência no dia ruim.',
  },
  {
    icon: 'gem',
    title: 'Elo ranqueado',
    text: 'Do Ferro à Lenda: cada tarefa vale XP, e cada elo novo libera uma roupa que não se compra.',
  },
  {
    icon: 'star',
    title: 'Títulos de RPG',
    text: 'A academia treina Força, o estudo treina Inteligência. Treine e vire "Mestre nos estudos".',
  },
  {
    icon: 'coin',
    title: 'Quarto que evolui',
    text: 'Começa com um colchão e um celular. Cada melhoria muda o quarto e rende mais moedas na categoria dela.',
  },
  {
    icon: 'chest',
    title: 'Baú da semana',
    text: 'Quanto mais dias cumpridos, melhor o baú de segunda: madeira, prata, ouro ou lendário.',
  },
]

const DEMO_DECOR = ['poster_space', 'lamp_desk', 'plant_small', 'rug_round', 'chair_gamer']
const DEMO_EQUIPMENT = { COMPUTER: 3, DESK: 3, BED: 2, BOOKSHELF: 2, GYM: 1, PEACE: 1, ORGANIZER: 2 } as const

/**
 * Página pública do GasmTask (/bem-vindo): o que é o app, como funciona e os planos. É a porta de entrada do
 * produto como serviço; quem já tem sessão vê o botão de voltar ao app.
 */
export function LandingPage() {
  const { state } = useAuth()
  const signedIn = state.status === 'authenticated'

  return (
    <div className={styles.page}>
      <PageTitle title="Sua rotina vira um jogo" />
      <header className={styles.top}>
        <Wordmark />
        {signedIn ? (
          <ButtonLink to="/" variant="secondary" className={styles.topButton}>
            Abrir o app
          </ButtonLink>
        ) : (
          <ButtonLink to="/entrar" variant="secondary" className={styles.topButton}>
            Entrar
          </ButtonLink>
        )}
      </header>

      <main className={styles.main}>
        <section className={styles.hero} aria-labelledby="hero-title">
          <div className={styles.heroText}>
            <p className={styles.kicker}>Produtividade com cara de jogo</p>
            <h1 id="hero-title" className={styles.headline}>
              Sua rotina vira um jogo.
            </h1>
            <p className={styles.lead}>
              Estudar, treinar, ler e dormir no horário valem XP, moedas e dias seguidos. Suba de elo, ganhe títulos e
              monte seu quarto em pixel art.
            </p>
            <div className={styles.cta}>
              {signedIn ? (
                <ButtonLink to="/">Continuar jogando</ButtonLink>
              ) : (
                <>
                  <ButtonLink to="/cadastro">Criar conta grátis</ButtonLink>
                  <ButtonLink to="/entrar" variant="secondary">
                    Já tenho conta
                  </ButtonLink>
                </>
              )}
            </div>
          </div>
          <HeroScene />
        </section>

        <section className={styles.section} aria-labelledby="how-title">
          <h2 id="how-title" className={styles.sectionTitle}>
            Como funciona
          </h2>
          <ol className={styles.steps}>
            <li>
              <strong>Crie suas missões.</strong> Academia às 18h, 1 hora de estudo, ler 20 páginas, dormir às 23h.
            </li>
            <li>
              <strong>Cumpra no dia.</strong> No horário ou com foto, a recompensa é maior.
            </li>
            <li>
              <strong>Evolua.</strong> Veja semana a semana quanto você treinou e estudou, e gaste as moedas na loja.
            </li>
          </ol>
        </section>

        <section className={styles.section} aria-labelledby="features-title">
          <h2 id="features-title" className={styles.sectionTitle}>
            O que tem no jogo
          </h2>
          <ul className={styles.features}>
            {FEATURES.map((feature) => (
              <li key={feature.title} className={styles.feature}>
                <span className={styles.featureIcon} aria-hidden="true">
                  <PixelIcon name={feature.icon} />
                </span>
                <h3 className={styles.featureTitle}>{feature.title}</h3>
                <p className={styles.featureText}>{feature.text}</p>
              </li>
            ))}
          </ul>
        </section>

        <section className={styles.section} aria-labelledby="room-title">
          <h2 id="room-title" className={styles.sectionTitle}>
            Seu quarto, do seu jeito
          </h2>
          <RoomScene
            equipment={DEMO_EQUIPMENT}
            items={DEMO_DECOR}
            wearing={['headphones_basic', 'hoodie_purple']}
            period="SUNSET"
            label="Um quarto montado no GasmTask"
          />
        </section>

        <section className={styles.section} aria-labelledby="plans-title">
          <h2 id="plans-title" className={styles.sectionTitle}>
            Planos
          </h2>
          <ul className={styles.plans}>
            <li className={styles.plan}>
              <h3 className={styles.planName}>Grátis</h3>
              <p className={styles.price}>R$ 0</p>
              <ul className={styles.perks}>
                <li>Missões, sequência, elo e títulos</li>
                <li>Loja, quarto e baú semanal</li>
                <li>Estatísticas e evolução por missão</li>
                <li>App instalável no celular</li>
              </ul>
              {!signedIn && (
                <ButtonLink to="/cadastro" block>
                  Começar grátis
                </ButtonLink>
              )}
            </li>
            <li className={styles.plan} data-soon>
              <h3 className={styles.planName}>
                Pro <span className={styles.soon}>em breve</span>
              </h3>
              <p className={styles.price}>Em breve</p>
              <ul className={styles.perks}>
                <li>Temporadas ranqueadas com recompensas</li>
                <li>Temas de quarto e roupas exclusivas</li>
                <li>Estatísticas avançadas e exportação</li>
                <li>Lembretes por notificação no celular</li>
              </ul>
            </li>
          </ul>
        </section>
      </main>

      <footer className={styles.footer}>
        <Wordmark />
        <nav className={styles.footerLinks} aria-label="Conta">
          <Link to="/entrar">Entrar</Link>
          <Link to="/cadastro">Criar conta</Link>
        </nav>
      </footer>
    </div>
  )
}

/** Cena da capa: o mascote no gramado, o baú de ouro e moedas no ar. */
function HeroScene() {
  const player = useMemo(() => dressCharacter(['cap_red', 'scarf_knit']), [])
  const chest = useMemo(() => chestSprite('GOLD'), [])
  return (
    <div className={styles.sceneFrame}>
      <PixelScene width={96} height={48} className={styles.scene} label="O mascote do GasmTask ao lado de um baú de ouro">
        <rect width={96} height={18} fill="#73d6ff" />
        <rect y={18} width={96} height={14} fill="#9fe6ff" />
        <rect y={32} width={96} height={8} fill="#cdf5ff" />
        <Placed sprite={SUN} x={78} y={4} />
        <Placed sprite={CLOUD} x={6} y={7} className={styles.cloud} />
        <Placed sprite={CLOUD} x={44} y={14} className={styles.cloudSlow} />
        <rect y={39} width={96} height={1} fill="#1a1c2c" />
        <rect y={40} width={96} height={2} fill="#a7f070" />
        <rect y={42} width={96} height={6} fill="#38b764" />
        <Placed sprite={player} x={28} y={20} className={styles.hop} />
        <Placed sprite={chest} x={50} y={27} />
        <Placed sprite={ICONS.coin} x={54} y={14} className={styles.coinA} />
        <Placed sprite={ICONS.coin} x={64} y={9} className={styles.coinB} />
      </PixelScene>
    </div>
  )
}
