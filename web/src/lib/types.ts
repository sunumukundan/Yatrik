/**
 * The shapes the backend actually returns.
 *
 * These mirror the Java records one-for-one. Where a Java field is `long` the TS type
 * is `number`; Java's `long` is 64-bit and JSON numbers are IEEE-754 doubles, but
 * paise amounts stay well inside the 2^53 safe-integer range (₹90 trillion), so no
 * precision is lost in practice.
 *
 * Amounts are ALWAYS paise. Never divide by 100 before formatting — use the helpers
 * in lib/money.ts.
 */

export type VehicleStatus = 'ACTIVE' | 'IDLE' | 'IN_SERVICE' | 'RETIRED'
export type DriverStatus = 'ACTIVE' | 'ON_TRIP' | 'INACTIVE'
export type TripStatus = 'PLANNED' | 'RUNNING' | 'COMPLETED' | 'CANCELLED'
export type MaintenanceType = 'ROUTINE_SERVICE' | 'REPAIR' | 'BODY_WORK' | 'TYRES' | 'OVERHAUL'
export type WorkOrderStatus = 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
export type ServiceScheduleStatus = 'PENDING' | 'DUE' | 'SERVICED'
export type DocType = 'RC' | 'INSURANCE' | 'PERMIT' | 'FITNESS' | 'PUC' | 'TAX' | 'LICENCE'
export type ExpenseCategory =
  | 'FUEL'
  | 'TOLL'
  | 'DRIVER_BATTA'
  | 'REPAIR'
  | 'LOADING'
  | 'POLICE'
  | 'OTHER'
export type PaidByType = 'DRIVER' | 'OWNER'
export type VehicleExpenseCategory = 'EMI' | 'INSURANCE_PREMIUM' | 'SERVICING' | 'OTHER'
export type PartyType = 'CUSTOMER' | 'SUPPLIER' | 'DRIVER'
export type EntryType = 'DEBIT' | 'CREDIT'
export type PaymentMode = 'CASH' | 'UPI' | 'BANK' | 'CHEQUE'
export type ReminderChannel = 'WHATSAPP' | 'SMS' | 'PUSH'
export type ReminderStatus = 'PENDING' | 'SENT' | 'FAILED'
export type UserRole = 'SUPER_ADMIN' | 'ADMIN' | 'FLEET_OWNER' | 'DRIVER' | 'ACCOUNTANT'
export type OrgStatus = 'TRIAL' | 'ACTIVE' | 'GRACE' | 'SUSPENDED' | 'CANCELLED'
export type SubscriptionPlan = 'FREE' | 'STARTER' | 'GROWTH' | 'ENTERPRISE'

// ---------------------------------------------------------------- auth

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  userId: string
  organisationId: string
  role: UserRole
}

// ---------------------------------------------------------------- fleet

export interface Vehicle {
  id: string
  regNo: string
  type: string
  makeModel: string | null
  capacityTons: number | null
  status: VehicleStatus
  currentDriverId: string | null
  lastServiceOdometerKm: number | null
}

export interface VehicleCreate {
  regNo: string
  type: string
  makeModel?: string | null
  capacityTons?: number | null
}

export interface VehicleUpdate {
  makeModel?: string | null
  capacityTons?: number | null
  status?: VehicleStatus
}

export interface Driver {
  id: string
  name: string
  phone: string
  licenceNo: string | null
  licenceExpiry: string | null
  status: DriverStatus
}

export interface DriverCreate {
  name: string
  phone: string
  licenceNo?: string | null
  licenceExpiry?: string | null
}

export interface DriverUpdate {
  name?: string | null
  phone?: string | null
  licenceNo?: string | null
  licenceExpiry?: string | null
  status?: DriverStatus
}

export interface VehicleDocument {
  id: string
  vehicleId: string
  docType: DocType
  docNo: string | null
  issueDate: string | null
  expiryDate: string | null
  fileId: string | null
}

export interface VehicleDocumentCreate {
  docType: DocType
  docNo?: string | null
  issueDate?: string | null
  expiryDate?: string | null
  fileId?: string | null
}

export interface DriverDocument {
  id: string
  driverId: string
  docType: DocType
  expiryDate: string | null
  fileId: string | null
}

export interface MaintenanceRecord {
  id: string
  vehicleId: string
  type: MaintenanceType
  description: string
  dueDate: string | null
  dueOdometerKm: number | null
  status: ServiceScheduleStatus
  servicedOn: string | null
  servicedAtOdometerKm: number | null
  costAmount: number | null
  garageName: string | null
  notes: string | null
}

export interface MaintenanceCreate {
  type: MaintenanceType
  description: string
  dueDate?: string | null
  dueOdometerKm?: number | null
  notes?: string | null
}

export interface MaintenanceService {
  servicedOn?: string | null
  odometerKm?: number | null
  costAmount?: number | null
  garageName?: string | null
}

export interface WorkOrder {
  id: string
  vehicleId: string
  maintenanceId: string | null
  type: MaintenanceType
  description: string
  status: WorkOrderStatus
  raisedOn: string | null
  scheduledFor: string | null
  startedAt: string | null
  completedAt: string | null
  odometerKm: number | null
  costAmount: number | null
  garageName: string | null
  notes: string | null
}

export interface WorkOrderCreate {
  type: MaintenanceType
  description: string
  maintenanceId?: string | null
  scheduledFor?: string | null
  odometerKm?: number | null
  costAmount?: number | null
  garageName?: string | null
  notes?: string | null
}

export interface WorkOrderUpdate {
  status?: WorkOrderStatus
  costAmount?: number | null
  odometerKm?: number | null
}

export interface FleetSummary {
  totalVehicles: number
  activeVehicles: number
  idleVehicles: number
  inServiceVehicles: number
  retiredVehicles: number
  totalDrivers: number
  availableDrivers: number
  driversOnTrip: number
  openWorkOrders: number
  servicesDueSoon: number
}

// ---------------------------------------------------------------- trips

export interface Trip {
  id: string
  vehicleId: string
  driverId: string
  partyId: string | null
  origin: string
  destination: string
  startDate: string | null
  endDate: string | null
  distanceKm: number | null
  freightAmount: number
  customerAdvance: number
  driverAdvance: number
  status: TripStatus
  notes: string | null
  closedAt: string | null
}

export interface TripCreate {
  vehicleId: string
  driverId: string
  partyId?: string | null
  origin: string
  destination: string
  startDate?: string | null
  distanceKm?: number | null
  freightAmount: number
  customerAdvance?: number
  driverAdvance?: number
  notes?: string | null
}

export interface TripUpdate {
  distanceKm?: number | null
  endDate?: string | null
  freightAmount?: number | null
  customerAdvance?: number | null
  driverAdvance?: number | null
  notes?: string | null
  status?: TripStatus
}

export interface TripProfit {
  tripId: string
  freightAmount: number
  totalExpenses: number
  profit: number
}

export interface TripDriverCash {
  tripId: string
  driverAdvance: number
  spentByDriver: number
  balanceWithDriver: number
}

export interface TripExpense {
  id: string
  tripId: string
  category: ExpenseCategory
  amount: number
  paidBy: PaidByType
  expenseDate: string | null
  note: string | null
}

export interface TripExpenseCreate {
  category: ExpenseCategory
  amount: number
  paidBy: PaidByType
  expenseDate?: string | null
  receiptFileId?: string | null
  note?: string | null
  clientRef: string
}

export interface VehicleExpense {
  id: string
  vehicleId: string
  category: VehicleExpenseCategory
  amount: number
  expenseDate: string | null
  note: string | null
}

export interface VehicleExpenseCreate {
  category: VehicleExpenseCategory
  amount: number
  expenseDate?: string | null
  note?: string | null
  clientRef?: string | null
}

export interface TripSummaryRow {
  tripId: string
  vehicleId: string
  driverId: string
  partyId: string | null
  startDate: string | null
  endDate: string | null
  distanceKm: number | null
  freightAmount: number
  customerAdvance: number
  driverAdvance: number
  totalExpenses: number
  profit: number
  spentByDriver: number
}

// ---------------------------------------------------------------- ledger

export interface Party {
  id: string
  name: string
  phone: string | null
  type: PartyType
  gstNo: string | null
  openingBalance: number
  currentBalance: number
}

export interface PartyCreate {
  name: string
  phone?: string | null
  type: PartyType
  gstNo?: string | null
  openingBalance: number
}

export interface PartyUpdate {
  name?: string | null
  phone?: string | null
  gstNo?: string | null
}

export interface LedgerEntry {
  id: string
  partyId: string
  tripId: string | null
  entryType: EntryType
  amount: number
  entryDate: string | null
  mode: PaymentMode | null
  reference: string | null
  note: string | null
}

export interface LedgerEntryCreate {
  entryType: EntryType
  amount: number
  entryDate?: string | null
  tripId?: string | null
  mode?: PaymentMode | null
  reference?: string | null
  note?: string | null
  clientRef?: string | null
}

export interface PaymentReminder {
  id: string
  partyId: string
  dueDate: string | null
  amount: number
  channel: ReminderChannel | null
  status: ReminderStatus
  sentAt: string | null
}

export interface PaymentReminderCreate {
  partyId: string
  amount: number
  dueDate?: string | null
  channel?: ReminderChannel | null
}

// ---------------------------------------------------------------- reports

export interface UtilizationReport {
  totalVehicles: number
  vehiclesInService: number
  vehiclesIdle: number
  vehiclesOffRoad: number
  tripsRun: number
  completedTrips: number
  totalDistanceKm: number
  averageDistancePerTripKm: number
  utilizationPercent: number
}

export interface FinancialReport {
  totalFreight: number
  totalTripExpenses: number
  totalProfit: number
  customerAdvanceReceived: number
  driverAdvanceGiven: number
  outstandingReceivables: number
  marginPercent: number
}

export interface FuelSpendReport {
  totalExpenses: number
  totalDistanceKm: number
  costPerKm: number
  tripsCount: number
}

export interface ReceivableRow {
  partyId: string
  partyName: string
  partyType: string
  amountDue: number
}
