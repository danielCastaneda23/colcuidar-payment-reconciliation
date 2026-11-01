package com.reconcilitation.reconcilitation.controllers.mappers;

import java.util.List;

import org.springframework.stereotype.Component;

import com.reconcilitation.reconcilitation.controllers.dto.ScheduledTurnResponse;
import com.reconcilitation.reconcilitation.model.ScheduleReadResult;

/**
 * 
 * ScheduleMapper handles to transfer information between components with loose coupling.
 * ! it use @Component decorator, it means we are adding a new component to our ioc set for
 * ! dependency injection on other components (Spring Boot tool)
 */
@Component
public class ScheduleMapper {
    /**
     * uploadResponse handles model result and turn it into a data transfer object to send back to the client.
     * we are using as model entities as dto.
     * @param result model entity result from service's response
     * @return DTO to transfer back to client
     */
    public ScheduledTurnResponse uploadResponse(ScheduleReadResult result) {
        List<ScheduledTurnResponse.RejectedRowResponse> rejected = result.rejectedRows().stream()
                .map(r -> new ScheduledTurnResponse.RejectedRowResponse(r.rowNumber(), r.reason()))
                .toList();
        return new ScheduledTurnResponse(result.turns().size(), rejected.size(), rejected);
    }
}
