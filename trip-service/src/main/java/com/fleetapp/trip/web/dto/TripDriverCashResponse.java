package com.fleetapp.trip.web.dto;

import java.util.UUID;

/**
 * Where the driver's cash stands for one trip (all paise).
 * balanceWithDriver = driverAdvance - spentByDriver:
 *   positive: the driver still holds that much of the owner's cash
 *   negative: the driver spent their own money; the owner owes them that much
 */
public record TripDriverCashResponse(
        UUID tripId, long driverAdvance, long spentByDriver, long balanceWithDriver
) {}
