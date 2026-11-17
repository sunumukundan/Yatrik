-- Milestone 1: Roles, Licensing, and White-label
-- Add white-label, license entitlement, and fleet assignment fields

-- Organisation table additions
ALTER TABLE organisation ADD COLUMN IF NOT EXISTS display_name VARCHAR(200);
ALTER TABLE organisation ADD COLUMN IF NOT EXISTS logo_url VARCHAR(500);
ALTER TABLE organisation ADD COLUMN IF NOT EXISTS primary_color VARCHAR(7);
ALTER TABLE organisation ADD COLUMN IF NOT EXISTS accent_color VARCHAR(7);
ALTER TABLE organisation ADD COLUMN IF NOT EXISTS support_email VARCHAR(150);
ALTER TABLE organisation ADD COLUMN IF NOT EXISTS support_phone VARCHAR(20);

-- License entitlement fields
ALTER TABLE organisation ADD COLUMN IF NOT EXISTS license_admin_seats INTEGER DEFAULT 1;
ALTER TABLE organisation ADD COLUMN IF NOT EXISTS license_fleet_owner_seats INTEGER DEFAULT 1;
ALTER TABLE organisation ADD COLUMN IF NOT EXISTS license_start_date TIMESTAMP WITH TIME ZONE;
ALTER TABLE organisation ADD COLUMN IF NOT EXISTS license_end_date TIMESTAMP WITH TIME ZONE;
ALTER TABLE organisation ADD COLUMN IF NOT EXISTS license_grace_period_days INTEGER DEFAULT 7;

-- Feature flags
ALTER TABLE organisation ADD COLUMN IF NOT EXISTS feature_drivers_billable BOOLEAN DEFAULT FALSE;
ALTER TABLE organisation ADD COLUMN IF NOT EXISTS feature_custom_domain BOOLEAN DEFAULT FALSE;
ALTER TABLE organisation ADD COLUMN IF NOT EXISTS feature_branded_sender BOOLEAN DEFAULT FALSE;

-- AppUser table additions
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS assigned_fleet_id UUID;
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS license_seat_active BOOLEAN DEFAULT FALSE;

-- Add foreign key for assigned_fleet_id (self-referencing to organisation for fleet support, or to a fleet table)
-- For now, just add the column; foreign key will be added when fleet service is updated

-- Indexes for license queries
CREATE INDEX IF NOT EXISTS idx_organisation_status ON organisation(status);
CREATE INDEX IF NOT EXISTS idx_organisation_license_end ON organisation(license_end_date);
CREATE INDEX IF NOT EXISTS idx_app_user_assigned_fleet ON app_user(assigned_fleet_id);
CREATE INDEX IF NOT EXISTS idx_app_user_license_seat ON app_user(license_seat_active);

-- Update existing OWNER roles to ADMIN (since OWNER no longer exists)
UPDATE app_user SET role = 'ADMIN' WHERE role = 'OWNER';

-- Update existing organisations: set default license seats based on plan
UPDATE organisation SET
    license_admin_seats = CASE
        WHEN plan = 'ENTERPRISE' THEN 10
        WHEN plan = 'GROWTH' THEN 3
        WHEN plan = 'STARTER' THEN 1
        ELSE 1
    END,
    license_fleet_owner_seats = CASE
        WHEN plan = 'ENTERPRISE' THEN 20
        WHEN plan = 'GROWTH' THEN 5
        WHEN plan = 'STARTER' THEN 2
        ELSE 1
    END;