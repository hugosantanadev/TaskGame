import type { CSSProperties } from 'react'

import type { Attribute, AttributeStatus, CharacterView, TitleCode } from '../../api/types'
import { asApiError } from '../../api/errors'
import { Button, ButtonLink } from '../../components/Button'
import { LockIcon } from '../../components/gameIcons'
import { PageTitle } from '../../components/PageTitle'
import { BOOK, LAPTOP } from '../../game/pixel/art/scenery'
import { Avatar } from '../../game/pixel/Avatar'
import { wornCodes } from '../../game/pixel/items'
import { PixelIcon, PixelSprite } from '../../game/pixel/PixelSprite'
import { ATTRIBUTE_LABELS, attributeColor, levelProgress, trainedBy } from '../../lib/attributes'
import { SLOT_LABEL, STATE_LABEL } from '../../lib/collection'
import { isMasterTitle, nextTitle, titleLabel } from '../../lib/titles'
import { ItemSticker } from '../store/ItemSticker'
import { useCharacter, useChooseTitle, useUnequip } from '../store/storeApi'
import styles from './Collection.module.css'
import page from './CharacterPage.module.css'

export function CharacterPage() {
  const character = useCharacter()
  const unequip = useUnequip()
  const choose = useChooseTitle()
  const error = character.error ?? unequip.error ?? choose.error

  return (
    <div className={styles.page}>
      <PageTitle title="Personagem" />
      <ButtonLink to="/perfil" variant="quiet" className={styles.back}>
        Voltar ao perfil
      </ButtonLink>
      <h1 className={styles.heading}>Personagem</h1>
      {character.isPending && <p className={styles.note}>Carregando…</p>}
      {error && (
        <p className={styles.error} role="alert">
          {asApiError(error).message}
        </p>
      )}
      {character.data && (
        <>
          <Stage character={character.data} />

          <ul className={page.slots} aria-label="O que está vestindo">
            {character.data.slots.map(({ slot, item }) => (
              <li key={slot} className={page.slot}>
                {item ? <ItemSticker item={item.item} /> : <span className={styles.emptySlot} aria-hidden="true" />}
                <span className={page.slotName}>{SLOT_LABEL[slot]}</span>
                <span className={page.slotItem}>{item ? item.item.name : 'Vazio'}</span>
                {item && (
                  <Button
                    variant="quiet"
                    disabled={unequip.isPending}
                    onClick={() => unequip.mutate(slot)}
                    aria-label={`Tirar: ${item.item.name}`}
                  >
                    Tirar
                  </Button>
                )}
              </li>
            ))}
          </ul>
          <ButtonLink to="/loja?visao=colecao" variant="secondary" block>
            Trocar de roupa na coleção
          </ButtonLink>

          <TitlesSection character={character.data} pending={choose.isPending} onChoose={(code) => choose.mutate(code)} />

          <section className={styles.sheet} aria-labelledby="ficha">
            <h2 id="ficha" className={styles.sheetTitle}>
              Ficha
            </h2>
            <p className={styles.note}>Cada categoria de tarefa treina um atributo. Treino só soma.</p>
            <ul className={styles.attributes}>
              {character.data.attributes.map((status) => (
                <li key={status.attribute}>
                  <AttributeRow status={status} character={character.data} />
                </li>
              ))}
            </ul>
          </section>
        </>
      )}
    </div>
  )
}

/** O personagem num palco de pixel, com o título por cima e o que está fazendo agora. */
function Stage({ character }: { character: CharacterView }) {
  const { state, activeTitle } = character
  const prop = state === 'AT_COMPUTER' ? LAPTOP : state === 'STUDYING' || state === 'READING' ? BOOK : null
  return (
    <section className={page.stage} aria-label="Seu personagem">
      {activeTitle && (
        <p className={page.plate} data-master={isMasterTitle(activeTitle) || undefined}>
          <PixelIcon name="star" /> {titleLabel(activeTitle)}
        </p>
      )}
      <div className={page.figure}>
        <Avatar wearing={wornCodes(character)} sleeping={state === 'SLEEPING'} scale={8} className={page.avatar} />
        {prop && <PixelSprite sprite={prop} scale={4} className={page.prop} />}
      </div>
      <p className={page.state}>
        Agora: <strong>{STATE_LABEL[state].toLowerCase()}</strong>
      </p>
    </section>
  )
}

const ATTRIBUTE_ORDER: Attribute[] = ['INTELLIGENCE', 'STRENGTH', 'WISDOM', 'SPIRIT', 'VITALITY', 'CREATIVITY', 'DISCIPLINE']

/** Títulos ganhos treinando atributos; tocar num ganho passa a exibi-lo no topo do dia e no ranking. */
function TitlesSection({
  character,
  pending,
  onChoose,
}: {
  character: CharacterView
  pending: boolean
  onChoose: (code: TitleCode | null) => void
}) {
  const unlocked = character.titles.filter((title) => title.unlocked).length
  return (
    <section className={styles.sheet} aria-labelledby="titulos">
      <div className={page.titlesHead}>
        <h2 id="titulos" className={styles.sheetTitle}>
          Títulos
        </h2>
        <span className={page.count}>
          {unlocked} de {character.titles.length}
        </span>
      </div>
      <p className={styles.note}>Treine um atributo até os níveis 3, 6 e 10 para ganhar os títulos dele.</p>
      <ul className={page.titleGroups}>
        {ATTRIBUTE_ORDER.map((attribute) => (
          <li key={attribute}>
            <h3 className={page.groupName}>{ATTRIBUTE_LABELS[attribute]}</h3>
            <ul className={page.titles}>
              {character.titles
                .filter((title) => title.attribute === attribute)
                .map((title) => {
                  const active = character.activeTitle === title.code
                  return (
                    <li key={title.code}>
                      {title.unlocked ? (
                        <button
                          type="button"
                          className={page.title}
                          data-master={isMasterTitle(title.code) || undefined}
                          aria-pressed={active}
                          disabled={pending}
                          onClick={() => onChoose(active ? null : title.code)}
                        >
                          {titleLabel(title.code)}
                        </button>
                      ) : (
                        <span className={page.title} data-locked>
                          <LockIcon aria-hidden="true" /> Nv {title.level}
                          <span className="visually-hidden">: {titleLabel(title.code)}, ainda não ganho</span>
                        </span>
                      )}
                    </li>
                  )
                })}
            </ul>
          </li>
        ))}
      </ul>
      {character.activeTitle && (
        <Button variant="quiet" disabled={pending} onClick={() => onChoose(null)}>
          Não mostrar título
        </Button>
      )}
    </section>
  )
}

function AttributeRow({ status, character }: { status: AttributeStatus; character: CharacterView }) {
  const name = ATTRIBUTE_LABELS[status.attribute]
  const next = nextTitle(character.titles, status.attribute)
  return (
    <div className={styles.attribute} style={{ '--attribute': attributeColor(status) } as CSSProperties}>
      <span className={styles.attributeName}>{name}</span>
      <span className={styles.attributeLevel}>Nv {status.level}</span>
      <span
        className={styles.attributeBar}
        role="progressbar"
        aria-label={`${name}: nível ${status.level}`}
        aria-valuemin={status.levelStartXp}
        aria-valuemax={status.nextLevelXp}
        aria-valuenow={status.xp}
      >
        <span style={{ width: `${levelProgress(status) * 100}%` }} />
      </span>
      <span className={styles.attributeMeta}>
        {trainedBy(status)} · {status.xp}/{status.nextLevelXp} XP
        {next && ` · ${titleLabel(next.code)} no nível ${next.level}`}
      </span>
    </div>
  )
}
