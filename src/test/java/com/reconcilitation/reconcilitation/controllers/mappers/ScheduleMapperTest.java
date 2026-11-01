package com.reconcilitation.reconcilitation.controllers.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.reconcilitation.reconcilitation.controllers.dto.ScheduledTurnResponse;
import com.reconcilitation.reconcilitation.model.RejectedRow;
import com.reconcilitation.reconcilitation.model.ScheduleReadResult;
import com.reconcilitation.reconcilitation.model.ScheduledTurn;
import com.reconcilitation.reconcilitation.model.enums.Shift;
class ScheduleMapperTest {

    private final ScheduleMapper mapper = new ScheduleMapper();

    private static ScheduledTurn turn(String nurse) {
        return new ScheduledTurn(LocalDate.of(2026, 10, 1), Shift.DAY, nurse, 95_000);
    }

    @Test
    void countsValidTurnsAndRejectedRows() {
        ScheduleReadResult result = new ScheduleReadResult(
                List.of(turn("PEREZ ANA"), turn("GOMEZ LUIS")),
                List.of(new RejectedRow(5, "date not readable: '5//10/2026'")));

        ScheduledTurnResponse response = mapper.uploadResponse(result);

        assertEquals(new ScheduledTurnResponse(
                2,
                1,
                List.of(new ScheduledTurnResponse.RejectedRowResponse(5, "date not readable: '5//10/2026'"))),
                response);
    }

    @Test
    void mapsEveryRejectedRowKeepingItsRowNumberAndReason() {
        ScheduleReadResult result = new ScheduleReadResult(
                List.of(),
                List.of(new RejectedRow(3, "no shift"),
                        new RejectedRow(9, "nurse name is empty")));

        ScheduledTurnResponse response = mapper.uploadResponse(result);

        assertEquals(new ScheduledTurnResponse(
                0,
                2,
                List.of(new ScheduledTurnResponse.RejectedRowResponse(3, "no shift"),
                        new ScheduledTurnResponse.RejectedRowResponse(9, "nurse name is empty"))),
                response);
    }

    @Test
    void returnsZeroCountsForAnEmptyResult() {
        ScheduledTurnResponse response = mapper.uploadResponse(new ScheduleReadResult(List.of(), List.of()));

        assertEquals(new ScheduledTurnResponse(0, 0, List.of()), response);
    }

    @Test
    void reportsNoRejectionsWhenAllRowsAreValid() {
        ScheduledTurnResponse response = mapper.uploadResponse(
                new ScheduleReadResult(List.of(turn("PEREZ ANA")), List.of()));

        assertEquals(new ScheduledTurnResponse(1, 0, List.of()), response);
    }
}
