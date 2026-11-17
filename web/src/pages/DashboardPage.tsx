/**
 * The dashboard.
 *
 * Answers the four questions an operator opens the app to ask:
 *   1. What did I earn this month?
 *   2. What is stuck — work orders, expiring documents, overdue payments?
 *   3. Which vehicles are earning and which are off the road?
 *   4. What needs my attention right now?
 *
 * The figures come from the reporting service, which aggregates across trip, fleet
 * and ledger — so this screen never has to stitch three calls together by hand.
 */

import { Link } from 'react-router-dom'
import {
  ArrowRight,
  FileWarning,
  Receipt,
  Wallet,
  Wrench,
} from 'lucide-react'
import {
  useDueReminders,
  useExpiringVehicleDocuments,
  useFinancials,
  useFleetSummary,
  useFuelSpend,
  useOpenWorkOrders,
  useReceivables,
  useTrips,
  useUtilization,
  useVehicles,
} from '../lib/queries'
import { startOfMonth, today } from '../lib/api'
import { formatCompact, formatMoney, groupIndian } from '../lib/money'
import { label } from '../lib/labels'
import {
  Badge,
  EmptyState,
  ErrorState,
  LoadingState,
  PageHeader,
  SectionTitle,
  StatTile,
  Table,
  Td,
  Th,
} from '../components/ui'

export function DashboardPage() {
  const from = startOfMonth()
  const to = today()

  const fleet = useFleetSummary()
  const financials = useFinancials(from, to)
  const utilization = useUtilization(from, to)
  const fuel = useFuelSpend(from, to)
  const receivables = useReceivables()
  const workOrders = useOpenWorkOrders()
  const expiringDocs = useExpiringVehicleDocuments(30)
  const dueReminders = useDueReminders(7)

  const loading = fleet.isLoading || financials.isLoading
  const error = fleet.error ?? financials.error

  // Check if other services are available (not 404/connection refused)
  const fleetStatus = fleet.error && typeof fleet.error === 'object' && 'status' in fleet.error ? (fleet.error as { status: number }).status : null
  const financialsStatus = financials.error && typeof financials.error === 'object' && 'status' in financials.error ? (financials.error as { status: number }).status : null
  const servicesUnavailable = fleetStatus === 404 || financialsStatus === 404

  if (loading) return <LoadingState label="Loading your dashboard…" />
  if (error && !servicesUnavailable) {
    return (
      <div className="space-y-4">
        <PageHeader title="Dashboard" />
        <ErrorState message={String(error.message)} onRetry={() => fleet.refetch()} />
      </div>
    )
  }
  // If services unavailable, treat as empty data and render empty dashboard

  const fin = financials.data
  const util = utilization.data
  const summary = fleet.data
  const receivablesTotal = (receivables.data ?? []).reduce((sum, r) => sum + r.amountDue, 0)

  return (
    <div className="space-y-6">
      <PageHeader
        title="Dashboard"
        description={`${prettyDate(from)} – ${prettyDate(to)}`}
        actions={
          <Link to="/reports" className="btn-secondary">
            Full reports
            <ArrowRight className="h-4 w-4" aria-hidden />
          </Link>
        }
      />

      {/* --- the money --- */}
      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatTile
          label="Freight earned"
          value={formatCompact(fin?.totalFreight)}
          sub={`${groupIndian((fin?.totalFreight ?? 0) / 100)} rupees`}
          tone="info"
        />
        <StatTile
          label="Trip expenses"
          value={formatCompact(fin?.totalTripExpenses)}
          sub="Fuel, tolls, batta"
        />
        <StatTile
          label="Profit"
          value={formatCompact(fin?.totalProfit)}
          sub={`${fin?.marginPercent ?? 0}% margin on freight`}
          tone={(fin?.totalProfit ?? 0) >= 0 ? 'success' : 'danger'}
        />
        <StatTile
          label="Outstanding receivables"
          value={formatCompact(receivablesTotal)}
          sub={`${receivables.data?.length ?? 0} ${receivables.data?.length === 1 ? 'customer' : 'customers'} owe you`}
          tone={receivablesTotal > 0 ? 'warning' : 'neutral'}
        />
      </section>

      {/* --- the fleet --- */}
      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatTile
          label="Vehicles on the road"
          value={`${summary?.activeVehicles ?? 0} / ${summary?.totalVehicles ?? 0}`}
          sub={`${summary?.idleVehicles ?? 0} idle, ${summary?.inServiceVehicles ?? 0} off the road`}
        />
        <StatTile
          label="Utilisation"
          value={`${util?.utilizationPercent ?? 0}%`}
          sub={`${util?.tripsRun ?? 0} trips, ${util?.completedTrips ?? 0} completed`}
          tone="info"
        />
        <StatTile
          label="Drivers available"
          value={`${summary?.availableDrivers ?? 0} / ${summary?.totalDrivers ?? 0}`}
          sub={`${summary?.driversOnTrip ?? 0} on a trip right now`}
        />
        <StatTile
          label="Fuel cost per km"
          value={fuel.data?.costPerKm ? `₹${fuel.data.costPerKm}` : '—'}
          sub={`${fuel.data?.tripsCount ?? 0} trips, ${fuel.data?.totalDistanceKm ?? 0} km`}
        />
      </section>

      {/* --- what needs attention --- */}
      <section className="grid gap-4 lg:grid-cols-2">
        <AttentionCard
          title="Work orders"
          icon={<Wrench className="h-4 w-4" aria-hidden />}
          count={workOrders.data?.length ?? 0}
          emptyText="No open work orders. Everything is running."
          link="/maintenance"
          linkText="Open maintenance"
        >
          {(workOrders.data ?? []).slice(0, 4).map((wo) => (
            <li key={wo.id} className="flex items-center justify-between gap-3 py-2">
              <div className="min-w-0">
                <p className="truncate text-sm font-medium text-ink-800">{wo.description}</p>
                <p className="text-xs text-ink-500">
                  {label.maintenanceType(wo.type)}
                  {wo.garageName ? ` · ${wo.garageName}` : ''}
                </p>
              </div>
              <Badge tone="danger">{label.workOrderStatus(wo.status)}</Badge>
            </li>
          ))}
        </AttentionCard>

        <AttentionCard
          title="Documents expiring"
          icon={<FileWarning className="h-4 w-4" aria-hidden />}
          count={expiringDocs.data?.length ?? 0}
          emptyText="All vehicle documents are valid."
          link="/compliance"
          linkText="Open compliance"
        >
          {(expiringDocs.data ?? []).slice(0, 4).map((doc) => (
            <li key={doc.id} className="flex items-center justify-between gap-3 py-2">
              <div className="min-w-0">
                <p className="truncate text-sm font-medium text-ink-800">
                  {label.docType(doc.docType)}
                </p>
                <p className="text-xs text-ink-500">
                  {doc.docNo ? `${doc.docNo} · ` : ''}Expires {prettyDate(doc.expiryDate)}
                </p>
              </div>
              <Badge tone="warning">{daysUntil(doc.expiryDate)}d</Badge>
            </li>
          ))}
        </AttentionCard>

        <AttentionCard
          title="Payments due"
          icon={<Receipt className="h-4 w-4" aria-hidden />}
          count={dueReminders.data?.length ?? 0}
          emptyText="No payments due this week."
          link="/ledger"
          linkText="Open ledger"
        >
          {(dueReminders.data ?? []).slice(0, 4).map((r) => (
            <li key={r.id} className="flex items-center justify-between gap-3 py-2">
              <div className="min-w-0">
                <p className="truncate text-sm font-medium text-ink-800">
                  {formatMoney(r.amount)}
                </p>
                <p className="text-xs text-ink-500">Due {prettyDate(r.dueDate)}</p>
              </div>
              <Badge tone="warning">{label.reminderStatus(r.status)}</Badge>
            </li>
          ))}
        </AttentionCard>

        <AttentionCard
          title="Who owes you"
          icon={<Wallet className="h-4 w-4" aria-hidden />}
          count={receivables.data?.length ?? 0}
          emptyText="Nothing outstanding. Every customer has paid."
          link="/ledger"
          linkText="Open ledger"
        >
          {(receivables.data ?? []).slice(0, 4).map((r) => (
            <li key={r.partyId} className="flex items-center justify-between gap-3 py-2">
              <div className="min-w-0">
                <p className="truncate text-sm font-medium text-ink-800">{r.partyName}</p>
                <p className="text-xs text-ink-500">{label.partyType(r.partyType)}</p>
              </div>
              <span className="shrink-0 text-sm font-semibold tnum text-ink-900">
                {formatMoney(r.amountDue)}
              </span>
            </li>
          ))}
        </AttentionCard>
      </section>

      {/* --- recent trips --- */}
      <section className="card overflow-hidden">
        <div className="border-b border-ink-200 px-4 py-3">
          <SectionTitle
            action={
              <Link to="/trips" className="btn-ghost">
                All trips
                <ArrowRight className="h-4 w-4" aria-hidden />
              </Link>
            }
          >
            Recent trips
          </SectionTitle>
        </div>
        <RecentTrips />
      </section>
    </div>
  )
}

function AttentionCard({
  title,
  icon,
  count,
  emptyText,
  link,
  linkText,
  children,
}: {
  title: string
  icon: React.ReactNode
  count: number
  emptyText: string
  link: string
  linkText: string
  children: React.ReactNode
}) {
  return (
    <div className="card p-4">
      <div className="mb-2 flex items-center justify-between gap-2">
        <h2 className="flex items-center gap-2 text-base font-semibold text-ink-900">
          {icon}
          {title}
        </h2>
        {count > 0 && (
          <Badge tone={count > 0 ? 'warning' : 'neutral'}>
            {count} {count === 1 ? 'item' : 'items'}
          </Badge>
        )}
      </div>

      {count === 0 ? (
        <p className="py-4 text-sm text-ink-500">{emptyText}</p>
      ) : (
        <>
          <ul className="divide-y divide-ink-100">{children}</ul>
          <Link to={link} className="btn-ghost mt-2 w-full">
            {linkText}
            <ArrowRight className="h-4 w-4" aria-hidden />
          </Link>
        </>
      )}
    </div>
  )
}

/** The last few trips, straight from the trips list. */
function RecentTrips() {
  const trips = useTrips()
  const vehicles = useVehicles()

  if (trips.isLoading) return <LoadingState />
  if (trips.error) return <div className="p-4"><ErrorState message={trips.error.message} /></div>

  const rows = (trips.data ?? []).slice(0, 6)
  if (rows.length === 0) {
    return (
      <EmptyState
        title="No trips yet"
        description="Record your first trip to start tracking freight, expenses and profit."
        action={
          <Link to="/trips" className="btn-primary">
            Add a trip
          </Link>
        }
      />
    )
  }

  const regOf = (id: string) =>
    vehicles.data?.find((v) => v.id === id)?.regNo ?? '—'

  return (
    <Table>
      <thead className="bg-ink-50">
        <tr>
          <Th>Route</Th>
          <Th>Vehicle</Th>
          <Th>Status</Th>
          <Th className="text-right">Freight</Th>
        </tr>
      </thead>
      <tbody className="divide-y divide-ink-100">
        {rows.map((t) => (
          <tr key={t.id}>
            <Td>
              <Link to={`/trips/${t.id}`} className="font-medium text-brand-700 hover:underline">
                {t.origin} → {t.destination}
              </Link>
            </Td>
            <Td>{regOf(t.vehicleId)}</Td>
            <Td>
              <Badge tone={TRIP_TONE[t.status] ?? 'neutral'}>{label.tripStatus(t.status)}</Badge>
            </Td>
            <Td numeric>{formatMoney(t.freightAmount)}</Td>
          </tr>
        ))}
      </tbody>
    </Table>
  )
}

const TRIP_TONE: Record<string, 'neutral' | 'success' | 'warning' | 'danger' | 'info'> = {
  PLANNED: 'neutral',
  RUNNING: 'info',
  COMPLETED: 'success',
  CANCELLED: 'danger',
}

/** "12 Jan 2026" — or a dash when the date is missing. */
function prettyDate(iso: string | null | undefined): string {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })
}

/** Whole days until a date; negative when it has passed. */
function daysUntil(iso: string | null | undefined): number | null {
  if (!iso) return null
  const target = new Date(iso)
  if (Number.isNaN(target.getTime())) return null
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  target.setHours(0, 0, 0, 0)
  return Math.round((target.getTime() - now.getTime()) / 86_400_000)
}
