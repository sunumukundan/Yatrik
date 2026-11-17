package com.fleetapp.fleet.domain;

/** ON_TRIP is set while a driver is on a running trip, so they cannot be double-booked. */
public enum DriverStatus { ACTIVE, ON_TRIP, INACTIVE }
