/** Formatos das respostas da API (espelham os DTOs do backend). */

export type User = {
  id: string
  email: string
  displayName: string
  timeZone: string
  rankingVisible: boolean
  createdAt: string
}

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

export type Streak = { current: number; longest: number; todayStatus: TodayStatus; lastFulfilledDate: string | null }

export type Today = {
  date: string
  timeOfDay: Period
  occurrences: Occurrence[]
  nextOccurrenceId: string | null
  progress: Progress
  walletBalance: number
  streak: Streak
  onboarding: boolean
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
}

export type ProofAttached = { occurrence: Occurrence; proofBonus: number; walletBalance: number }

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
export type CharacterView = { state: CharacterState; slots: { slot: CharacterSlot; item: InventoryItem | null }[] }
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
  achievementsUnlocked: number
  completedByCategory: { category: TaskCategory; completed: number }[]
}

export type SummaryDayStatus = 'FULFILLED' | 'FAILED' | 'REST' | 'PENDING' | 'UPCOMING'

export type WeekSummary = {
  weekStart: string
  weekEnd: string
  finished: boolean
  totals: Totals
  fulfilledDays: number
  days: { date: string; dayOfWeek: DayOfWeek; status: SummaryDayStatus; totals: Totals }[]
  achievements: UnlockedAchievement[]
}

/** Página da API (PageResponse do backend). */
export type Page<T> = { content: T[]; page: number; size: number; totalElements: number; totalPages: number }

export type RankingMetric = 'POINTS' | 'COMPLETED_TASKS' | 'COINS_EARNED' | 'STREAK'
export type RankingEntry = { position: number; displayName: string; value: number; you: boolean }

/** `me.position` é onde a pessoa está (ou estaria, se oculta) entre os visíveis; null sem pontuação. */
export type Ranking = {
  period: 'WEEK'
  metric: RankingMetric
  scope: 'GLOBAL'
  from: string
  to: string
  entries: Page<RankingEntry>
  me: { position: number | null; value: number; visible: boolean }
}
