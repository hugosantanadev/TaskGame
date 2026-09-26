import { PageTitle } from '../../components/PageTitle'
import styles from './PlaceholderPage.module.css'

/** Telas cuja função ainda não foi implementada: dizem o que vão mostrar. */
export function PlaceholderPage({ title, description }: { title: string; description: string }) {
  return (
    <div className={styles.page}>
      <PageTitle title={title} />
      <h1 className={styles.title}>{title}</h1>
      <p className={styles.description}>{description}</p>
      <p className={styles.status}>Em construção.</p>
    </div>
  )
}
