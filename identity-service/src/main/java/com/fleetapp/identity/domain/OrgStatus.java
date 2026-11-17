package com.fleetapp.identity.domain;

/**
 * Organisation lifecycle status.
 *
 * - TRIAL: New organisations start here with limited features
 * - ACTIVE: Fully licensed and operational
 * - GRACE: License expired but within grace period
 * - SUSPENDED: Explicitly suspended by Super Admin
 * - CANCELLED: Permanently cancelled
 */
public enum OrgStatus {
    TRIAL,
    ACTIVE,
    GRACE,
    SUSPENDED,
    CANCELLED
}
