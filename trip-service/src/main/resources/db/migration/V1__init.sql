-- Trip Service schema
-- Owns: trips, trip expenses, non-trip vehicle expenses
-- Money stored as BIGINT paise. client_ref is the offline idempotency key.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE trip_status AS ENUM ('PLANNED', 'RUNNING', 'COMPLETED', 'CANCELLED');
CREATE TYPE expense_category AS ENUM ('FUEL', 'TOLL', 'DRIVER_BATTA', 'REPAIR', 'LOADING', 'POLICE', 'OTHER');
CREATE TYPE paid_by_type AS ENUM ('DRIVER', 'OWNER');
CREATE TYPE vehicle_expense_category AS ENUM ('EMI', 'INSURANCE_PREMIUM', 'SERVICING', 'OTHER');

CREATE TABLE trip (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    vehicle_id      UUID NOT NULL,
    driver_id       UUID NOT NULL,
    party_id        UUID,                          -- customer, from Ledger service
    origin          VARCHAR(150) NOT NULL,
    destination     VARCHAR(150) NOT NULL,
    start_date      DATE,
    end_date        DATE,
    distance_km     NUMERIC(8,2),
    freight_amount  BIGINT NOT NULL DEFAULT 0,      -- paise
    customer_advance BIGINT NOT NULL DEFAULT 0,     -- paise, see V2__split_advance
    driver_advance  BIGINT NOT NULL DEFAULT 0,      -- paise, see V2__split_advance
    currency        VARCHAR(3) NOT NULL DEFAULT 'INR',
    status          trip_status NOT NULL DEFAULT 'PLANNED',
    notes           TEXT,
    closed_at       TIMESTAMPTZ,                    -- set when COMPLETED; locks numbers
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    deleted_at      TIMESTAMPTZ,
    version         INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE trip_expense (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    trip_id         UUID NOT NULL REFERENCES trip(id),
    category        expense_category NOT NULL,
    amount          BIGINT NOT NULL,                -- paise
    paid_by         paid_by_type NOT NULL,
    expense_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    receipt_file_id UUID,
    note            TEXT,
    client_ref      UUID NOT NULL,                  -- idempotency key from mobile app
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    deleted_at      TIMESTAMPTZ,
    version         INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_trip_expense_client_ref UNIQUE (organisation_id, client_ref)
);

CREATE TABLE vehicle_expense (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    vehicle_id      UUID NOT NULL,
    category        vehicle_expense_category NOT NULL,
    amount          BIGINT NOT NULL,
    expense_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    note            TEXT,
    client_ref      UUID,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    deleted_at      TIMESTAMPTZ,
    version         INTEGER NOT NULL DEFAULT 0
);

-- Outbox table for reliable event publishing (same-transaction write)
CREATE TABLE outbox_event (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(50) NOT NULL,           -- e.g. 'TRIP'
    aggregate_id    UUID NOT NULL,
    event_type      VARCHAR(100) NOT NULL,          -- e.g. 'TripCompleted'
    payload         JSONB NOT NULL,
    organisation_id UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ
);

CREATE INDEX idx_trip_org_status ON trip (organisation_id, status, created_at);
CREATE INDEX idx_trip_vehicle ON trip (vehicle_id, start_date);
CREATE INDEX idx_trip_driver ON trip (driver_id, start_date);
CREATE INDEX idx_trip_expense_trip ON trip_expense (trip_id, expense_date);
CREATE INDEX idx_vehicle_expense_vehicle ON vehicle_expense (vehicle_id, expense_date);
CREATE INDEX idx_outbox_unpublished ON outbox_event (published_at) WHERE published_at IS NULL;
