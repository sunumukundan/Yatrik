package com.fleetapp.trip.web.dto;

import java.util.UUID;

public record TripProfitResponse(
        UUID tripId, long freightAmount, long totalExpenses, long profit
) {}
