package com.reconcilitation.reconcilitation.model;

import java.time.LocalDate;

import com.reconcilitation.reconcilitation.model.enums.Shift;

/**
 * 
 * ScheduledTurn is turn container entity
 * @param date save date of turn
 * @param shift kind of shift
 * @param nurseName name of nurse assistant
 * @param value value of shift
 */
public record ScheduledTurn(LocalDate date, Shift shift, String nurseName, long value) {

}
