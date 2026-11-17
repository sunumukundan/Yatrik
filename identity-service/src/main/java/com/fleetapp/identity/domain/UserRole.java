package com.fleetapp.identity.domain;

/**
 * Platform roles mapped to the Milestone 1 roadmap.
 *
 * - SUPER_ADMIN: Platform-wide administrator (not tenant-scoped)
 * - ADMIN: Organisation administrator (tenant-scoped)
 * - FLEET_OWNER: Fleet owner with license (tenant-scoped, fleet-assigned)
 * - DRIVER: Driver (tenant-scoped, trip-assigned)
 * - ACCOUNTANT: Finance/accounting role (tenant-scoped)
 */
public enum UserRole {
    SUPER_ADMIN,
    ADMIN,
    FLEET_OWNER,
    DRIVER,
    ACCOUNTANT
}
