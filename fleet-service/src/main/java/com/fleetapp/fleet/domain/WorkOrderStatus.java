package com.fleetapp.fleet.domain;

/**
 * OPEN:       raised, work not started.
 * IN_PROGRESS: a garage has the vehicle and the work has begun.
 * COMPLETED:  work finished. This is what moves the vehicle back into service.
 * CANCELLED:  raised in error, or the vehicle was scrapped instead.
 */
public enum WorkOrderStatus { OPEN, IN_PROGRESS, COMPLETED, CANCELLED }
