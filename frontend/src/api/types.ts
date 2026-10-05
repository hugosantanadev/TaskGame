/** Formatos das respostas da API (espelham os DTOs do backend). */

export type User = {
  id: string
  email: string
  displayName: string
  timeZone: string
  rankingVisible: boolean
  createdAt: string
  /** Código do título exibido (ex.: STUDY_MASTER); null sem título. */
  activeTitle: TitleCode | null
  plan: Plan
}

/** Plano da conta. Hoje todo mundo é FREE e os limites estão desligados; o PRO é a base do SaaS. */
export type Plan = 'FREE' | 'PRO'

export type AuthResponse = {
  accessToken: string
  tokenType: 'Bearer'
  expiresAt: string
  user: User
}

export type FieldViolation = {
  field: string
  message: string
}

/** Problem Details (RFC 9457) com o `code` estável do GasmTask. */
export type ProblemDetails = {
  type?: string
  title?: string
  status?: number
  detail?: string
  code?: string
  errors?: FieldViolation[]
}

// ---------------------------------------------------------------- Fase 2: missões, plano e conclusão

export type TaskCategory = 'STUDY' | 'READING' | 'SPIRITUALITY' | 'EXERCISE' | 'SLEEP' | 'PROJECT' | 'HOME' | 'OTHER'
export type TaskKind = 'MANDATORY' | 'EXTRA'
export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY'
export type OccurrenceStatus = 'PENDING' | 'COMPLETED' | 'MISSED'
export type TodayStatus = 'FULFILLED' | 'PENDING' | 'REST'
export type Period = 'MORNING' | 'AFTERNOON' | 'SUNSET' | 'NIGHT'

/** Horários chegam como "HH:mm:ss"; `null` é "sem horário". */
export type ScheduleEntry = { dayOfWeek: DayOfWeek; time: string | null }

export type Mission = {
  id: string
  name: string
  description: string | null
  category: TaskCategory
  kind: TaskKind
  points: number
  coins: number
  durationMinutes: number | null
  requiresProof: boolean
  schedule: ScheduleEntry[]
  archived: boolean
  createdAt: string
}

export type MissionInput = {
  name: string
  description: string | null
  category: TaskCategory
  kind: TaskKind
  points: number | null
  durationMinutes: number | null
  requiresProof: boolean
  schedule: ScheduleEntry[]
  startToday: boolean
}

export type Occurrence = {
  id: string
  taskId: string | null
  date: string
  plannedTime: string | null
  title: string
  category: TaskCategory
  kind: TaskKind
  points: number
  coins: number
  durationMinutes: number | null
  requiresProof: boolean
  status: OccurrenceStatus
  completedAt: string | null
  onTime: boolean | null
  earnedPoints: number | null
  earnedCoins: number | null
  proofAttached: boolean
  removable: boolean
}

export type DayPlan = {
  date: string
  dayOfWeek: DayOfWeek
  today: boolean
  past: boolean
  canAddMandatory: boolean
  canAddExtra: boolean
  occurrences: Occurrence[]
}

export type Week = { weekStart: string; weekEnd: string; current: boolean; editable: boolean; days: DayPlan[] }

export type Progress = {
  mandatoryPlanned: number
  mandatoryDone: number
  extrasPlanned: number
  extrasDone: number
  points: number
  coins: number
  fulfilled: boolean
}

/** `freezes`: protetores de sequência guardados (até `maxFreezes`); `lastFrozenDate`: último dia salvo por um. */
export type Streak = {
  current: number
  longest: number
  todayStatus: TodayStatus
  lastFulfilledDate: string | null
  freezes: number
  maxFreezes: number
  freezePrice: number
  lastFrozenDate: string | null
}

export type Today = {
  date: string
  timeOfDay: Period
  occurrences: Occurrence[]
  nextOccurrenceId: string | null
  progress: Progress
  walletBalance: number
  streak: Streak
  onboarding: boolean
  rank: RankStatus
  challenges: DailyChallenge[]
  /** Baú semanal ainda fechado; null se não houver. */
  pendingChest: Chest | null
}

export type ChallengeCode = 'EARLY_BIRD' | 'ON_TIME' | 'PHOTO' | 'EXTRA_MILE' | 'VARIETY' | 'FULL_DAY' | 'MARATHON'

/** Desafio do dia; `progress` vai até `target`. */
export type DailyChallenge = {
  code: ChallengeCode
  target: number
  progress: number
  completed: boolean
  xpReward: number
  coinReward: number
}

export type Reward = { points: number; baseCoins: number; onTimeBonus: number; proofBonus: number; totalCoins: number }

export type CompletionResult = {
  occurrence: Occurrence
  onTime: boolean
  reward: Reward
  walletBalance: number
  day: { status: TodayStatus; mandatoryDone: number; mandatoryPlanned: number }
  streak: { current: number; longest: number; increasedNow: boolean }
  unlockedAchievements: UnlockedAchievement[]
  xp: XpChange
  attribute: { gained: number; leveledUp: boolean; status: AttributeStatus; unlockedTitles: TitleCode[] }
  completedChallenges: DailyChallenge[]
}

export type ProofAttached = {
  occurrence: Occurrence
  proofBonus: number
  walletBalance: number
  completedChallenges: DailyChallenge[]
}

export type ExtraInput = {
  title: string
  category: TaskCategory
  points: number
  date: string
  time: string | null
  durationMinutes: number | null
  requiresProof: boolean
}

// ---------------------------------------------------------------- Fase 3: loja, coleção, quarto, personagem e conquistas

export type StoreItemCategory = 'FURNITURE' | 'DECORATION' | 'CHARACTER'
export type CharacterSlot = 'HEAD' | 'OUTFIT' | 'ACCESSORY'
export type CharacterState = 'IDLE' | 'STUDYING' | 'AT_COMPUTER' | 'READING' | 'SLEEPING'

export type StoreItem = {
  id: string
  code: string
  name: string
  description: string
  category: StoreItemCategory
  slot: CharacterSlot | null
  price: number
  assetKey: string | null
  owned: boolean
}

/** `id` é o item no inventário: é ele que vai para o quarto ou para o personagem. */
export type InventoryItem = {
  id: string
  item: StoreItem
  pricePaid: number
  acquiredAt: string
  inRoom: boolean
  equippedSlot: CharacterSlot | null
}

export type PurchaseResult = { inventoryItem: InventoryItem; walletBalance: number }
export type Room = { items: InventoryItem[] }
export type CharacterView = {
  state: CharacterState
  slots: { slot: CharacterSlot; item: InventoryItem | null }[]
  attributes: AttributeStatus[]
  titles: TitleStatus[]
  activeTitle: TitleCode | null
}

/** Títulos ganhos ao treinar atributos: três por atributo, nos níveis 3, 6 e 10. */
export type TitleCode =
  | 'STUDENT'
  | 'SHARP_MIND'
  | 'STUDY_MASTER'
  | 'GYM_REGULAR'
  | 'GYM_RAT'
  | 'STRENGTH_MASTER'
  | 'CURIOUS_READER'
  | 'BOOKWORM'
  | 'WISDOM_MASTER'
  | 'SERENE_SOUL'
  | 'STEADY_HEART'
  | 'SPIRIT_MASTER'
  | 'WELL_RESTED'
  | 'FULL_ENERGY'
  | 'REST_MASTER'
  | 'HANDS_ON'
  | 'BUILDER'
  | 'CREATOR_MASTER'
  | 'TIDY_HOME'
  | 'ORGANIZED'
  | 'DISCIPLINE_MASTER'

export type TitleStatus = { code: TitleCode; attribute: Attribute; level: number; unlocked: boolean }

export type Attribute = 'INTELLIGENCE' | 'STRENGTH' | 'WISDOM' | 'SPIRIT' | 'VITALITY' | 'CREATIVITY' | 'DISCIPLINE'

/** Um atributo na ficha: a barra vai de `levelStartXp` a `nextLevelXp`. */
export type AttributeStatus = {
  attribute: Attribute
  xp: number
  level: number
  levelStartXp: number
  nextLevelXp: number
  categories: TaskCategory[]
}
export type Wallet = { balance: number; totalEarned: number; totalSpent: number }
export type UnlockedAchievement = { code: string; name: string; description: string }

export type Achievement = {
  code: string
  name: string
  description: string
  criterion: string
  category: TaskCategory | null
  threshold: number
  progress: number
  unlocked: boolean
  unlockedAt: string | null
  assetKey: string | null
}

// ---------------------------------------------------------------- Fase 4: estatísticas, resumo semanal, ranking e game-state

/** Planejado × concluído de um período; `completionRate` em % inteira, null quando nada foi planejado. */
export type Totals = {
  mandatoryPlanned: number
  mandatoryDone: number
  extrasPlanned: number
  extrasDone: number
  planned: number
  done: number
  missed: number
  points: number
  coins: number
  completionRate: number | null
}

export type StatsGranularity = 'WEEK' | 'MONTH'
export type HistoryPeriod = { start: string; end: string; current: boolean; totals: Totals }
export type StatsHistory = { granularity: StatsGranularity; periods: HistoryPeriod[] }

export type StatsOverview = {
  completedTasks: number
  missedTasks: number
  completionRate: number | null
  points: number
  coinsEarned: number
  coinsSpent: number
  balance: number
  currentStreak: number
  longestStreak: number
  fulfilledDays: number
  failedDays: number
  restDays: number
  frozenDays: number
  achievementsUnlocked: number
  completedByCategory: { category: TaskCategory; completed: number }[]
}

export type SummaryDayStatus = 'FULFILLED' | 'FAILED' | 'REST' | 'FROZEN' | 'PENDING' | 'UPCOMING'

export type WeekSummary = {
  weekStart: string
  weekEnd: string
  finished: boolean
  totals: Totals
  fulfilledDays: number
  days: { date: string; dayOfWeek: DayOfWeek; status: SummaryDayStatus; totals: Totals }[]
  achievements: UnlockedAchievement[]
}

/** Uma missão vista de longe; `completionRate` null quando nada foi decidido ainda. */
export type MissionSummary = {
  taskId: string
  name: string
  category: TaskCategory
  archived: boolean
  completed: number
  missed: number
  completionRate: number | null
  currentStreak: number
  bestStreak: number
  lastCompletedDate: string | null
}

/** A evolução de uma missão semana a semana (ex.: idas à academia). `bestMonth` é o 1º dia do mês. */
export type MissionEvolution = {
  summary: MissionSummary
  weeks: HistoryPeriod[]
  bestMonth: string | null
  bestMonthCompleted: number
  weeklyAverage: number
  firstCompletedDate: string | null
}

/** Página da API (PageResponse do backend). */
export type Page<T> = { content: T[]; page: number; size: number; totalElements: number; totalPages: number }

export type RankingMetric = 'XP' | 'POINTS' | 'COMPLETED_TASKS' | 'COINS_EARNED' | 'STREAK'
export type RankingEntry = {
  position: number
  displayName: string
  title: TitleCode | null
  value: number
  rank: RankRef
  you: boolean
}

/** `me.position` é onde a pessoa está (ou estaria, se oculta) entre os visíveis; null sem pontuação. */
export type Ranking = {
  period: 'WEEK'
  metric: RankingMetric
  scope: 'GLOBAL'
  from: string
  to: string
  entries: Page<RankingEntry>
  me: { position: number | null; value: number; rank: RankRef; visible: boolean }
}

/** Estado consolidado para a futura camada visual (GET /me/game-state). `assetKey` é resolvida em sprite por ela. */
export type GameItem = { code: string; assetKey: string | null }

export type GameState = {
  timeOfDay: Period
  coins: number
  streak: { current: number; longest: number; todayStatus: TodayStatus }
  rank: RankStatus
  totals: { completedTasks: number; achievements: number }
  inventory: GameItem[]
  room: { items: GameItem[] }
  character: {
    state: CharacterState
    equipped: Partial<Record<CharacterSlot, GameItem>>
    attributes: Record<Attribute, number>
  }
}

/** Horários "HH:mm:ss" no fuso do usuário; null desliga aquele lembrete. */
export type ReminderSettings = { tasksEnabled: boolean; leadMinutes: number; bedtime: string | null; wakeTime: string | null }
export type ReminderKind = 'TASK' | 'BEDTIME' | 'WAKE_UP'
export type UpcomingReminder = {
  kind: ReminderKind
  notifyAt: string
  eventAt: string
  occurrenceId: string | null
  title: string | null
}

// ---------------------------------------------------------------- Elo ranqueado

export type RankTier = 'IRON' | 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM' | 'DIAMOND' | 'MASTER' | 'LEGEND'

/** `division` vai de 1 a 3; null em Lenda. */
export type RankRef = { tier: RankTier; division: number | null }

/** Elo atual e a barra até o próximo degrau (`nextRankXp` null em Lenda). */
export type RankStatus = RankRef & { xp: number; rankStartXp: number; nextRankXp: number | null }

export type XpChange = {
  gained: number
  status: RankStatus
  promoted: boolean
  demoted: boolean
  unlockedItems: StoreItem[]
}

export type XpReason = 'TASK_COMPLETED' | 'DAY_FULFILLED' | 'TASK_MISSED' | 'BACKFILL' | 'CHALLENGE_COMPLETED'

export type Progression = {
  status: RankStatus
  peakXp: number
  peakRank: RankRef
  ladder: { rank: RankRef; minXp: number }[]
  /** `item.owned` diz se a roupa do elo já foi desbloqueada. */
  rewards: { tier: RankTier; item: StoreItem }[]
  recent: { amount: number; reason: XpReason; title: string | null; eventDate: string | null; createdAt: string }[]
}

// ---------------------------------------------------------------- Baú semanal e conta

export type ChestTier = 'NONE' | 'WOOD' | 'SILVER' | 'GOLD' | 'LEGENDARY'

/** Baú da semana que começou em `weekStart`; o item exato só aparece ao abrir. */
export type Chest = {
  id: string
  weekStart: string
  tier: ChestTier
  fulfilledDays: number
  coins: number
  xp: number
  hasItem: boolean
  opened: boolean
}

/** `item` null quando o baú não trazia item ou quando ele virou moedas (a pessoa já tinha). */
export type OpenedChest = { chest: Chest; coins: number; item: StoreItem | null; xp: XpChange; walletBalance: number }
