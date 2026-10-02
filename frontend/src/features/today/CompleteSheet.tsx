import { useEffect, useMemo, useRef } from 'react'

import { asApiError } from '../../api/errors'
import type { Occurrence } from '../../api/types'
import { Button } from '../../components/Button'
import { CameraIcon, CheckIcon } from '../../components/gameIcons'
import { Sheet } from '../../components/Sheet'
import { formatTime, plural } from '../../lib/days'
import styles from './CompleteSheet.module.css'
import { useAttachProof, useCompleteOccurrence, useProofImage } from './todayApi'

type CompleteSheetProps = { occurrence: Occurrence | null; isToday: boolean; onClose: () => void }

/** Ações de uma tarefa do dia: concluir, concluir com foto, mandar a foto depois ou ver a foto enviada. */
export function CompleteSheet({ occurrence, isToday, onClose }: CompleteSheetProps) {
  return (
    <Sheet open={occurrence !== null} title={occurrence?.title ?? ''} onClose={onClose}>
      {occurrence && <Content key={occurrence.id} occurrence={occurrence} isToday={isToday} onDone={onClose} />}
    </Sheet>
  )
}

function Content({ occurrence, isToday, onDone }: { occurrence: Occurrence; isToday: boolean; onDone: () => void }) {
  const complete = useCompleteOccurrence()
  const attach = useAttachProof()
  const fileInput = useRef<HTMLInputElement>(null)
  const time = formatTime(occurrence.plannedTime)
  const error = complete.error ?? attach.error
  const busy = complete.isPending || attach.isPending

  const onPhoto = (file: File | undefined) => {
    if (!file) return
    if (occurrence.status === 'PENDING') complete.mutate({ occurrence, photo: file }, { onSuccess: onDone })
    else attach.mutate({ occurrence, photo: file }, { onSuccess: onDone })
  }

  return (
    <div className={styles.content}>
      <p className={styles.when}>
        {time ? `Planejada para ${time}` : 'Sem horário'}
        {occurrence.kind === 'EXTRA' ? ', extra' : ', obrigatória'}
      </p>

      {occurrence.status === 'PENDING' && isToday && (
        <>
          <p className={styles.value}>
            Vale {plural(occurrence.points, 'ponto', 'pontos')} e {plural(occurrence.coins, 'moeda', 'moedas')}. Ganha mais
            1 moeda com foto{occurrence.plannedTime ? ' e 1 se concluir perto do horário planejado' : ''}.
          </p>
          <div className={styles.actions}>
            {occurrence.requiresProof ? (
              <p className={styles.note}>Essa missão pede uma foto para ser concluída.</p>
            ) : (
              <Button block disabled={busy} onClick={() => complete.mutate({ occurrence }, { onSuccess: onDone })}>
                <CheckIcon /> Concluir
              </Button>
            )}
            <Button
              block
              variant={occurrence.requiresProof ? 'primary' : 'secondary'}
              disabled={busy}
              onClick={() => fileInput.current?.click()}
            >
              <CameraIcon /> {occurrence.requiresProof ? 'Enviar foto e concluir' : 'Concluir com foto'}
            </Button>
          </div>
        </>
      )}

      {occurrence.status === 'PENDING' && !isToday && (
        <p className={styles.note}>Só dá para concluir no próprio dia.</p>
      )}

      {occurrence.status === 'COMPLETED' && (
        <>
          <p className={styles.value}>
            Concluída{occurrence.onTime ? ' no horário' : ''}: +{plural(occurrence.earnedPoints ?? 0, 'ponto', 'pontos')} e +
            {plural(occurrence.earnedCoins ?? 0, 'moeda', 'moedas')}.
          </p>
          {occurrence.proofAttached ? (
            <ProofPreview occurrenceId={occurrence.id} />
          ) : (
            isToday && (
              <Button block variant="secondary" disabled={busy} onClick={() => fileInput.current?.click()}>
                <CameraIcon /> Adicionar foto (+1 moeda)
              </Button>
            )
          )}
        </>
      )}

      {occurrence.status === 'MISSED' && <p className={styles.note}>Essa tarefa ficou sem conclusão no dia dela.</p>}

      {busy && <p className={styles.note}>Enviando…</p>}
      {error && (
        <p className={styles.error} role="alert">
          {asApiError(error).message}
        </p>
      )}

      <input
        ref={fileInput}
        className="visually-hidden"
        type="file"
        accept="image/jpeg,image/png,image/webp"
        aria-label="Foto da tarefa"
        tabIndex={-1}
        onChange={(event) => {
          onPhoto(event.target.files?.[0])
          event.target.value = ''
        }}
      />
    </div>
  )
}

function ProofPreview({ occurrenceId }: { occurrenceId: string }) {
  const image = useProofImage(occurrenceId)
  const url = useMemo(() => (image.data ? URL.createObjectURL(image.data) : null), [image.data])
  useEffect(() => () => {
    if (url) URL.revokeObjectURL(url)
  }, [url])

  if (image.isError) return <p className={styles.note}>Não foi possível carregar a foto.</p>
  if (!url) return <p className={styles.note}>Carregando a foto…</p>
  return <img className={styles.photo} src={url} alt="Foto enviada como prova" />
}
