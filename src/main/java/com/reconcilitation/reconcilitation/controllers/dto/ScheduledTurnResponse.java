package com.reconcilitation.reconcilitation.controllers.dto;

import java.util.List;
/**
 * 
 * ScheduledTurnResponse is a DTO class to handle responses from model's services into server-client communication.
 * ! we are using record class because is useful to carry data between system's layers
 * @param turns number of shifts imported
 * @param rejectedCount number of rejected row from csv
 * @param rejectedRows information of rejected rows
 */
public record ScheduledTurnResponse(int turns, int rejectedCount, List<RejectedRowResponse> rejectedRows) {

    /**
     * 
     * RejectedRowResponse
     * @param row rejected row
     * @param reason reason of rejection
     */
    public record RejectedRowResponse(long row, String reason) {
    }
}
