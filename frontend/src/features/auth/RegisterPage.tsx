import { useState, type ChangeEvent, type FormEvent } from 'react'
import { Link } from 'react-router'

import { asApiError, type ApiError } from '../../api/errors'
import { useAuth } from '../../auth/context'
import { Button } from '../../components/Button'
import { Field, FormAlert } from '../../components/Field'
import { detectTimeZone } from '../../lib/datetime'
import styles from './AuthLayout.module.css'
import { AuthLayout } from './AuthLayout'

type RegisterForm = { displayName: string; email: string; password: string }

export function RegisterPage() {
  const { register } = useAuth()
  const [timeZone] = useState(detectTimeZone)
  const [form, setForm] = useState<RegisterForm>({ displayName: '', email: '', password: '' })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<ApiError | null>(null)

  const update = (field: keyof RegisterForm) => (event: ChangeEvent<HTMLInputElement>) =>
    setForm((current) => ({ ...current, [field]: event.target.value }))

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      await register({ ...form, timeZone })
    } catch (err) {
      setError(asApiError(err))
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout
      title="Criar conta"
      footer={
        <>
          Já tem conta? <Link to="/entrar">Entrar</Link>
        </>
      }
    >
      <form className={styles.form} onSubmit={handleSubmit}>
        {error && <FormAlert>{error.message}</FormAlert>}
        <Field
          label="Nome"
          name="displayName"
          autoComplete="nickname"
          required
          minLength={2}
          maxLength={40}
          hint="Aparece no app e no ranking."
          value={form.displayName}
          onChange={update('displayName')}
          error={error?.fieldErrors.displayName}
        />
        <Field
          label="E-mail"
          type="email"
          name="email"
          autoComplete="email"
          inputMode="email"
          required
          maxLength={254}
          value={form.email}
          onChange={update('email')}
          error={error?.fieldErrors.email}
        />
        <Field
          label="Senha"
          type="password"
          name="password"
          autoComplete="new-password"
          required
          minLength={8}
          hint="Pelo menos 8 caracteres."
          value={form.password}
          onChange={update('password')}
          error={error?.fieldErrors.password}
        />
        <p className={styles.note}>
          Fuso horário: <strong>{timeZone}</strong>, detectado neste aparelho. Dá para mudar depois no perfil.
        </p>
        {error?.fieldErrors.timeZone && <p className={styles.noteError}>{error.fieldErrors.timeZone}</p>}
        <Button type="submit" block disabled={submitting}>
          {submitting ? 'Criando conta…' : 'Criar conta'}
        </Button>
      </form>
    </AuthLayout>
  )
}
