package com.reconcilitation.reconcilitation.controllers;

import java.io.InputStream;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import com.reconcilitation.reconcilitation.controllers.dto.ScheduledTurnResponse;
import com.reconcilitation.reconcilitation.controllers.mappers.ScheduleMapper;
import com.reconcilitation.reconcilitation.model.interfaces.IScheduleReader;

import jakarta.validation.Valid;

/**
 * 
 * ScheduleController in charge of handling request either to back to client or to the model
 * abstraction. There will be 3 controllers to handle nusing operation team request.
 * 
 * !One important distinction - we are using decorator pattern here created by Spring boot framework
 * !to handle http server and request @RestController is a decorator which makes this class to be part of
 * ! IOC (INVERSION OF CONTROL) set and be available to call
 * !Second important distinction is @RequestMapping, this decorator is a mapper for routing our server requests
 * !directly to this class.
 */
@RestController
@RequestMapping("api/schedule")
public class ScheduleController {

    private final IScheduleReader reader;
    private final ScheduleMapper mapper;

    /**
     * Constructor class that initialize the schedule controller with an aggregation relationship
     * classes of mapper and reader model abstraction
     * @param mapper handle communication between the model responses and DTO information to forward to client
     * @param reader handle abstraction to execute model's services through abstractions
     */
    public ScheduleController(ScheduleMapper mapper, IScheduleReader reader) {
        this.mapper = mapper;
        this.reader = reader;
    }

    /**
     * uploadSchedule handles client request with CSV files
     * 
     * *Information for uploading csv took it from SpringBoot uploading in api docs
     * *https://spring.io/guides/gs/uploading-files
     * *also methods for turning into inputStream
     * *https://docs.spring.io/spring-framework/docs/7.0.x/javadoc-api/org/springframework/web/multipart/MultipartFile.html#getInputStream()
     * 
     * !Using Decorator pattern to establish specific endpoint map (@PostMapping("/upload") and also
     * !a response status.
     * @param file CSV Daily operation schedule file
     * @return JSON file which contains inserted rows and any possible rejected row.
     */
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduledTurnResponse uploadSchedule(@RequestParam("file") MultipartFile file) {
        //try-catch statement in case of reader service failure
        try {
            //convertin MultipartFile into InputStream to be able to csv reading
            InputStream dataToRead = file.getInputStream();

            //mapper to turn model response into dto (data transfer object) response.
            return mapper.uploadResponse(this.reader.result(dataToRead));

        } catch (Exception e) {
            // throw exception if result service failure
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read the uploaded file", e);

        }

    }
}
