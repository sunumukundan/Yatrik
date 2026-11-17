package com.fleetapp.fleet.domain;

/**
 * ACTIVE:   in service and available.
 * IDLE:     in service but not currently assigned to a trip.
 * IN_SERVICE: off the road for maintenance or repair. The vehicle earns nothing
 *             while in this state, which is what the utilisation report counts.
 * RETIRED:  sold or scrapped. Kept for history, not shown in the active fleet.
 */
public enum VehicleStatus { ACTIVE, IDLE, IN_SERVICE, RETIRED }
