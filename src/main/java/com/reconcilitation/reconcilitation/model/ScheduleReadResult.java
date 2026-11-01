package com.reconcilitation.reconcilitation.model;

import java.util.List;
/**
 * 
 * ScheduleReadResult entitie is in charge of having succesful and rejected uploaded turns
 * @param turns saved
 * @param rejectedRows rejected rows
 */
public record ScheduleReadResult(List<ScheduledTurn> turns, List<RejectedRow> rejectedRows) {

    /**
     * Consctructor create a new List of turns to avoid mutable object on the future
     * @param turns
     * @param rejectedRows
     */
    public ScheduleReadResult {
        turns = List.copyOf(turns);
        rejectedRows = List.copyOf(rejectedRows);
    }
}
