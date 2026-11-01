package com.reconcilitation.reconcilitation.model.interfaces;

import java.io.InputStream;

import com.reconcilitation.reconcilitation.model.ScheduleReadResult;

/**
 * 
 * IScheduleReader is the contract to implement on any service, only thing we care is the contract result method
 */
public interface IScheduleReader {

    //contract method to handle csv reading and creation of result entity.
    //this is only part of the application's domain
    ScheduleReadResult result(InputStream csvData);
}