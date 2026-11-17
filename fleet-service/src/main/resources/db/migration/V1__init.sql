-- Fleet Service schema
-- Owns: vehicles, drivers, their documents, and the maintenance schedule
-- organisation_id and user_id/driver links are foreign *conceptually* only
-- (Identity service owns app_user) — no cross-service FKs, validate via API/event.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE vehicle_status AS ENUM ('ACTIVE', 'IDLE', 'IN_SERVICE', 'RETIRED');
CREATE TYPE driver_status AS ENUM ('ACTIVE', 'ON_TRIP', 'INACTIVE');
CREATE TYPE doc_type AS ENUM ('RC', 'INSURANCE', 'PERMIT', 'FITNESS', 'PUC', 'TAX', 'LICENCE');
CREATE TYPE maintenance_type AS ENUM ('ROUTINE_SERVICE', 'REPAIR', 'BODY_WORK', 'TYRES', 'OVERHAUL');
CREATE TYPE work_order_status AS ENUM ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
CREATE TYPE service_schedule_status AS ENUM ('PENDING', 'DUE', 'SERVICED');

CREATE TABLE vehicle (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    reg_no          VARCHAR(20) NOT NULL,
    type            VARCHAR(50) NOT NULL,          -- e.g. TRUCK, TRAILER
    make_model      VARCHAR(100),
    capacity_tons   NUMERIC(6,2),
    status          vehicle_status NOT NULL DEFAULT 'IDLE',
    current_driver_id UUID,
    last_service_odometer_km NUMERIC(10,2),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    deleted_at      TIMESTAMPTZ,
    version         INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_vehicle_reg_org UNIQUE (organisation_id, reg_no)
);

CREATE TABLE driver (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    user_id         UUID,                          -- nullable: driver may not have app login yet
    name            VARCHAR(150) NOT NULL,
    phone           VARCHAR(15) NOT NULL,
    licence_no      VARCHAR(30),
    licence_expiry  DATE,
    status          driver_status NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    deleted_at      TIMESTAMPTZ,
    version         INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_driver_phone_org UNIQUE (organisation_id, phone)
);

ALTER TABLE vehicle
    ADD CONSTRAINT fk_vehicle_current_driver
    FOREIGN KEY (current_driver_id) REFERENCES driver(id);

CREATE TABLE vehicle_document (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    vehicle_id      UUID NOT NULL REFERENCES vehicle(id),
    doc_type        doc_type NOT NULL,
    doc_no          VARCHAR(50),
    issue_date      DATE,
    expiry_date     DATE,
    file_id         UUID,                          -- reference to File Service object
    reminder_sent_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    deleted_at      TIMESTAMPTZ,
    version         INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE driver_document (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    driver_id       UUID NOT NULL REFERENCES driver(id),
    doc_type        doc_type NOT NULL,
    expiry_date     DATE,
    file_id         UUID,
    reminder_sent_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    deleted_at      TIMESTAMPTZ,
    version         INTEGER NOT NULL DEFAULT 0
);

-- Maintenance schedule: what work is planned for a vehicle, and when it falls due.
-- A schedule entry may be driven by a date, by an odometer threshold, or both.
CREATE TABLE maintenance_record (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    vehicle_id      UUID NOT NULL REFERENCES vehicle(id),
    type            maintenance_type NOT NULL,
    description     VARCHAR(150) NOT NULL,
    due_date        DATE,
    due_odometer_km NUMERIC(10,2),
    status          service_schedule_status NOT NULL DEFAULT 'PENDING',
    serviced_on     DATE,
    serviced_at_odometer_km NUMERIC(10,2),
    cost_amount     BIGINT,                        -- paise
    garage_name     VARCHAR(150),
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    deleted_at      TIMESTAMPTZ,
    version         INTEGER NOT NULL DEFAULT 0
);

-- A work order is the execution of a piece of work. While one is OPEN or IN_PROGRESS
-- the vehicle is off the road (status IN_SERVICE).
CREATE TABLE work_order (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    vehicle_id      UUID NOT NULL REFERENCES vehicle(id),
    maintenance_id  UUID REFERENCES maintenance_record(id),
    type            maintenance_type NOT NULL,
    description     VARCHAR(150) NOT NULL,
    status          work_order_status NOT NULL DEFAULT 'OPEN',
    raised_on       DATE NOT NULL DEFAULT CURRENT_DATE,
    scheduled_for   DATE,
    started_at      TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    odometer_km     NUMERIC(10,2),
    cost_amount     BIGINT,                        -- paise
    garage_name     VARCHAR(150),
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    deleted_at      TIMESTAMPTZ,
    version         INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_vehicle_org ON vehicle (organisation_id, created_at);
CREATE INDEX idx_driver_org ON driver (organisation_id, created_at);
CREATE INDEX idx_vehicle_doc_expiry ON vehicle_document (vehicle_id, expiry_date);
CREATE INDEX idx_vehicle_doc_org_expiry ON vehicle_document (organisation_id, expiry_date);
CREATE INDEX idx_driver_doc_expiry ON driver_document (driver_id, expiry_date);
CREATE INDEX idx_maintenance_vehicle ON maintenance_record (vehicle_id, status);
CREATE INDEX idx_maintenance_due ON maintenance_record (organisation_id, due_date) WHERE status = 'PENDING';
CREATE INDEX idx_work_order_vehicle ON work_order (vehicle_id, status);
CREATE INDEX idx_work_order_open ON work_order (organisation_id, raised_on) WHERE status IN ('OPEN','IN_PROGRESS');
