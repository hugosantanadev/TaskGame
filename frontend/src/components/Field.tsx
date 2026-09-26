import { useId, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes } from 'react'

import styles from './Field.module.css'

type FieldChrome = { label: string; hint?: string; error?: string }

function useDescribedBy(id: string, hint?: string, error?: string) {
  if (error) return { describedBy: `${id}-error`, errorId: `${id}-error`, hintId: undefined }
  if (hint) return { describedBy: `${id}-hint`, errorId: undefined, hintId: `${id}-hint` }
  return { describedBy: undefined, errorId: undefined, hintId: undefined }
}

export function Field({ label, hint, error, id, ...input }: FieldChrome & InputHTMLAttributes<HTMLInputElement>) {
  const autoId = useId()
  const inputId = id ?? autoId
  const { describedBy, errorId, hintId } = useDescribedBy(inputId, hint, error)
  return (
    <div className={styles.field}>
      <label htmlFor={inputId} className={styles.label}>
        {label}
      </label>
      <input
        id={inputId}
        className={styles.input}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy}
        {...input}
      />
      {hintId && (
        <p id={hintId} className={styles.hint}>
          {hint}
        </p>
      )}
      {errorId && (
        <p id={errorId} className={styles.error}>
          {error}
        </p>
      )}
    </div>
  )
}

export function SelectField({
  label,
  hint,
  error,
  id,
  children,
  ...select
}: FieldChrome & SelectHTMLAttributes<HTMLSelectElement> & { children: ReactNode }) {
  const autoId = useId()
  const selectId = id ?? autoId
  const { describedBy, errorId, hintId } = useDescribedBy(selectId, hint, error)
  return (
    <div className={styles.field}>
      <label htmlFor={selectId} className={styles.label}>
        {label}
      </label>
      <select
        id={selectId}
        className={styles.input}
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy}
        {...select}
      >
        {children}
      </select>
      {hintId && (
        <p id={hintId} className={styles.hint}>
          {hint}
        </p>
      )}
      {errorId && (
        <p id={errorId} className={styles.error}>
          {error}
        </p>
      )}
    </div>
  )
}

/** Erro geral do formulário, anunciado por leitores de tela assim que aparece. */
export function FormAlert({ children }: { children: ReactNode }) {
  return (
    <p role="alert" className={styles.alert}>
      {children}
    </p>
  )
}
