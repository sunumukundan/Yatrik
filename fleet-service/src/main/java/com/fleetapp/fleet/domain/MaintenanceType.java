package com.fleetapp.fleet.domain;

/**
 * The kind of maintenance a work order or a schedule entry covers.
 */
public enum MaintenanceType {
    /** Scheduled service, driven by a date or an odometer threshold. */
    ROUTINE_SERVICE,
    /** A breakdown or defect found in the road. */
    REPAIR,
    /** Bodywork, tyres, glass, paint. */
    BODY_WORK,
    /** Tyre replacement, rotation, alignment. */
    TYRES,
    /** Overhaul scheduled by age or mileage, not by calendar. */
    OVERHAUL
}
