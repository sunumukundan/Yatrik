package com.fleetapp.trip.domain;

/**
 * Costs that belong to the vehicle rather than to one trip: the EMI, the insurance
 * premium, scheduled servicing. These are not trip expenses and never affect a
 * trip's profit.
 */
public enum VehicleExpenseCategory { EMI, INSURANCE_PREMIUM, SERVICING, OTHER }
