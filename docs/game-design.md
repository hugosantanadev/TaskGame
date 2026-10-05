# GasmTask: documento de design do jogo

O GasmTask é um app de produtividade com cara de jogo. Este documento descreve o jogo que fica em volta das tarefas: o que a pessoa vê, o que ela ganha e por que volta no dia seguinte. As regras de negócio e a arquitetura estão em [architecture.md](architecture.md).

## Referências

- **Vlogger Go Viral** (Tapps Games): o estúdio é a tela principal e o retrato do progresso; o equipamento tem função (melhora o vídeo) e a decoração é estilo; o assunto em alta rende mais; um café voador dá um impulso curto.
- **Finch**: o bichinho cresce em fases conforme a pessoa se cuida; com o dia completo ele sai numa aventura e volta com uma história; jornadas prontas tiram a pessoa da tela vazia; o tom é gentil, nada morre.

O que não copiamos: pagar para avançar (multiplicadores, toque automático, moedas à venda) e moeda por toque. Moeda e XP só vêm de tarefa real; dinheiro compra estilo, nunca progresso. E, ao contrário do Finch, o GasmTask mantém estatísticas fortes.

## O loop

```
tarefa real ──▶ moedas + XP por atributo
                 │
                 ├─▶ melhorias do quarto (bônus) e decoração (estilo)
                 ├─▶ corpo e carreira evoluem
                 └─▶ dia cumprido ──▶ salário + saída da noite (história, presente, dica de livro)
semana ──▶ baú e elo          estação ──▶ eventos com itens que só existem naquela época
```

## 1. O quarto como tela inicial

A tela Hoje abre com o quarto do personagem. Ele é o menu e o placar ao mesmo tempo: quem abre o app vê de cara o quanto já evoluiu.

**Começar do zero.** No primeiro dia o quarto tem um colchão no chão e um celular com teclado e mouse. Mais nada.

**Melhorias** (equipamento) são trilhas de três degraus. Cada degrau substitui o anterior e rende moedas a mais em toda tarefa concluída da categoria ligada a ele: +1 no primeiro degrau, +2 no segundo, +3 no terceiro. Só dá para comprar o próximo degrau.

| Trilha | Categoria | Degraus |
|---|---|---|
| Computador | Projeto | celular com teclado e mouse (início) → notebook velho → notebook novo → PC completo |
| Mesa | Estudo | chão (início) → mesa dobrável → escrivaninha → escrivaninha em L |
| Cama | Sono | colchão no chão (início) → cama de solteiro → cama aconchegante → cama com cabeceira |
| Estante | Leitura | pilha de livros (início) → prateleira → estante de madeira → estante dupla |
| Treino | Exercício | — → halteres → banco com halteres → rack com barra |
| Cantinho de paz | Espiritualidade | — → almofada → tapete com vela → cantinho com plantas |
| Organização | Casa e Outros | — → caixas → cômoda → guarda-roupa |

**Decoração** (pôster, planta, tapete, neon, aquário, pufe, cadeira, luminária, caneca) não dá bônus: é estilo, e a pessoa escolhe o que vai para o quarto.

**Hora do dia.** A janela mostra o céu do período e, à noite, o quarto escurece e a luminária acende.

**Visual.** Paleta mais sóbria, com cor viva só nos destaques (moedas, sequência, ação principal), e **modo noturno**: automático (segue o sistema), claro ou escuro, escolhido no perfil.

## 1b. Metas da semana (planejamento flexível)

Exemplo real: três frentes de estudo (faculdade, vibecoding para vender sites, Java para emprego), academia 5x e folga na segunda, que pode receber mais coisas.

- **Dias fixos ou meta da semana.** Ao criar a missão, uma escolha só: dias fixos (com horário) ou "N vezes por semana, eu escolho os dias".
- **Fazer hoje.** A tela Hoje mostra um painel compacto "Metas da semana" (Java 1/3, Academia 2/5) com o botão "Fazer hoje", que põe a meta no dia.
- **"Ou" dentro da meta.** Uma meta pode ter opções (Treino: academia ou cardio); ao concluir, a pessoa diz qual fez, e as estatísticas separam por opção.
- **Sequência sem armadilha.** O dia continua sendo cumprido pelas obrigatórias de dia fixo. A meta flexível é cobrada no fim da semana: bateu, ganha bônus; ficou abaixo, perde um pouco de XP, como uma obrigatória perdida. O que passa da meta conta como extra, com recompensa menor.

## Princípios de interface

- **Tela limpa.** Inspirado no jogo, mas sem poluição: pouca informação por cima do quarto, um painel por assunto.
- **Abas claras:** Hoje, Evolução, Loja, Ranking e Perfil, com o mesmo propósito de sempre.
- **Tutorial** (para depois de começar a vender): um passo a passo opcional na primeira vez, que pode ser pulado.

## 2. Corpo e carreira

**Corpo.** O personagem muda de físico com o nível de Força (academia): magrinho (níveis 1 e 2), em forma (3 a 5), atlético (6 a 9) e forte (10 em diante). O corpo nunca regride; quem sobe e desce é o elo.

**Carreira.** Sobe com o nível de Inteligência (estudo):

| Cargo | Inteligência | Salário por dia cumprido |
|---|---|---|
| Estudante | 1 | — |
| Estagiário | 3 | 3 moedas |
| Júnior | 5 | 6 moedas |
| Pleno | 7 | 10 moedas |
| Sênior | 10 | 15 moedas |

O salário entra na virada do dia, só se o dia foi cumprido: renda passiva que não substitui a tarefa. Cada promoção ganha comemoração. Depois do Júnior, o escritório (um segundo cenário) entra como compra grande.

## 3. Saída da noite

Quando o dia é cumprido, o personagem sai à noite e volta de manhã com uma história e um presente para pegar na tela Hoje. O destino vem do que mais foi feito no dia: academia (Exercício), biblioteca (Estudo), livraria (Leitura), estágio ou trabalho (Projeto), parque (Espiritualidade) e assim por diante. O presente é um punhado de moedas e, às vezes, uma decoração barata ou uma dica de livro.

## 4. Trilhas prontas

No primeiro acesso (e na tela de missões), pacotes que criam as missões sozinhos: Ficar forte, Passar na prova, Ler 12 livros no ano, Dormir melhor, Paz interior. Resolvem a tela vazia do dia 1.

## 5. Livros

- **Leitura atual:** busca do livro (Open Library), página atual e "terminei".
- **Estante que enche:** cada livro terminado vira uma lombada na estante do quarto.
- **Dicas de livro:** uma a cada nível de Sabedoria e nas saídas da noite, pelo assunto do que a pessoa lê e treina. O link de compra pode levar a tag de afiliado (configuração), que é uma das fontes de receita.

O Kindle não tem API pública de progresso de leitura; o registro é feito pela pessoa (página ou porcentagem), com foto opcional.

## 6. Temperos

- **Categoria em alta:** uma categoria por dia rende moedas a mais.
- **Café no quarto:** concluir no horário faz aparecer um café; tocar nele dá um bônus pequeno na próxima tarefa da hora seguinte.
- **Pet por indicação:** quem convida um amigo que cumpre o primeiro dia ganha um gato ou cachorro no quarto.
- **Eventos sazonais:** volta às aulas, Halloween, Natal, Carnaval, com itens que só existem naquela época.

## Monetização (depois)

Freemium com teste do Pro. O grátis tem o loop inteiro; o Pro traz profundidade e estilo (temas de quarto, escritório, roupas exclusivas, histórico longo, temporadas). Afiliados de livros entram pelas dicas. Nunca vender moedas, XP ou dados.
