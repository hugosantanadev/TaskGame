/** Título da aba do navegador (o React 19 leva o <title> para o <head>). */
export function PageTitle({ title }: { title: string }) {
  return <title>{`${title} | GasmTask`}</title>
}
