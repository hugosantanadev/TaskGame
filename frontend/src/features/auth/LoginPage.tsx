import { useState, type FormEvent } from 'react'
import { Link } from 'react-router'

import { asApiError, type ApiError } from '../../api/errors'
import { useAuth } from '../../auth/context'
import { Button } from '../../components/Button'
import { Field, FormAlert } from '../../components/Field'
import styles from './AuthLayout.module.css'
import { AuthLayout } from './AuthLayout'

export function LoginPage() {
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<ApiError | null>(null)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      await login({ email, password }) // com a sessão aberta, a rota redireciona sozinha
    } catch (err) {
      setError(asApiError(err))
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout
      title="Entrar"
      footer={
        <>
          Ainda não tem conta? <Link to="/cadastro">Criar conta</Link>
        </>
      }
    >
      <form className={styles.form} onSubmit={handleSubmit}>
        {error && <FormAlert>{error.message}</FormAlert>}
        <Field
          label="E-mail"
          type="email"
          name="email"
          autoComplete="email"
          inputMode="email"
          required
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          error={error?.fieldErrors.email}
        />
        <Field
          label="Senha"
          type="password"
          name="password"
          autoComplete="current-password"
          required
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          error={error?.fieldErrors.password}
        />
        <Button type="submit" block disabled={submitting}>
          {submitting ? 'Entrando…' : 'Entrar'}
        </Button>
      </form>
    </AuthLayout>
  )
}
