-- Ledger Service schema
-- Owns: parties (customers/suppliers/drivers as payees), ledger entries, reminders
-- party_balance is a cached read model, rebuildable from ledger_entry.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE party_type AS ENUM ('CUSTOMER', 'SUPPLIER', 'DRIVER');
CREATE TYPE entry_type AS ENUM ('DEBIT', 'CREDIT');   -- DEBIT: they owe us, CREDIT: they paid us
CREATE TYPE payment_mode AS ENUM ('CASH', 'UPI', 'BANK', 'CHEQUE');
CREATE TYPE reminder_channel AS ENUM ('WHATSAPP', 'SMS', 'PUSH');
CREATE TYPE reminder_status AS ENUM ('PENDING', 'SENT', 'FAILED');

CREATE TABLE party (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    name            VARCHAR(150) NOT NULL,
    phone           VARCHAR(15),
    type            party_type NOT NULL,
    gst_no          VARCHAR(20),
    opening_balance BIGINT NOT NULL DEFAULT 0,       -- paise, +ve = they owe us
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    deleted_at      TIMESTAMPTZ,
    version         INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE ledger_entry (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    party_id        UUID NOT NULL REFERENCES party(id),
    trip_id         UUID,                            -- nullable link to Trip service
    entry_type      entry_type NOT NULL,
    amount          BIGINT NOT NULL,                  -- paise, always positive
    entry_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    mode            payment_mode,
    reference       VARCHAR(100),                     -- cheque no, UTR, etc.
    note            TEXT,
    client_ref      UUID,                              -- idempotency key from mobile app
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    deleted_at      TIMESTAMPTZ,
    version         INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_ledger_client_ref UNIQUE (organisation_id, client_ref)
);

-- Cached read model. Deliberately minimal: no version, no soft delete. It is
-- overwritten in place on every entry and can be rebuilt from ledger_entry at any time.
CREATE TABLE party_balance (
    party_id        UUID PRIMARY KEY REFERENCES party(id),
    organisation_id UUID NOT NULL,
    balance         BIGINT NOT NULL DEFAULT 0,        -- +ve = they owe us, rebuildable
    last_entry_at   TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE payment_reminder (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    party_id        UUID NOT NULL REFERENCES party(id),
    due_date        DATE NOT NULL,
    amount          BIGINT NOT NULL,
    channel         reminder_channel NOT NULL DEFAULT 'WHATSAPP',
    sent_at         TIMESTAMPTZ,
    status          reminder_status NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    deleted_at      TIMESTAMPTZ,
    version         INTEGER NOT NULL DEFAULT 0
);

-- Outbox table for reliable event publishing (same-transaction write)
CREATE TABLE outbox_event (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(50) NOT NULL,
    aggregate_id    UUID NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         JSONB NOT NULL,
    organisation_id UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ
);

CREATE INDEX idx_party_org ON party (organisation_id, type);
CREATE INDEX idx_ledger_party_date ON ledger_entry (party_id, entry_date);
CREATE INDEX idx_ledger_org_date ON ledger_entry (organisation_id, entry_date);
CREATE INDEX idx_reminder_due ON payment_reminder (due_date, status);
CREATE INDEX idx_outbox_unpublished ON outbox_event (published_at) WHERE published_at IS NULL;
