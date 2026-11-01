/**
 * Code implement in this class was based on documentation of Common-csv in this link 
 * https://commons.apache.org/proper/commons-csv/apidocs/index.html
 * example : 
 * Reader in = new FileReader("path/to/file.csv");
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
        for (CSVRecord record : records) {
        String lastName = record.get("Last Name");
        String firstName = record.get("First Name");
        }
 * 
 */
package com.reconcilitation.reconcilitation.model.services.reader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import com.reconcilitation.reconcilitation.model.RejectedRow;
import com.reconcilitation.reconcilitation.model.ScheduleReadResult;
import com.reconcilitation.reconcilitation.model.ScheduledTurn;
import com.reconcilitation.reconcilitation.model.enums.Shift;
import com.reconcilitation.reconcilitation.model.interfaces.IScheduleReader;

/**
 * 
 * ScheduleReaderService is IScheduleReader contract implementation to handle CSV reading
 * !we use @Service decorator to put this class inside of IOC set.
 */
@Service
public class ScheduleReaderService implements IScheduleReader {

    private static final int COL_DATE = 0;
    private final DateTimeFormatter dateFormat;

    // we are formmating the initial argument date pattern to recieve certain dates format.
    public ScheduleReaderService() {
        //call the constructor itself to establish the date time pattern to follow
        this(DateTimeFormatter.ofPattern("d/M/[uuuu][uu]"));
    }

    public ScheduleReaderService(DateTimeFormatter dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**    (non-Javadoc)
     * result method implementation
     * @see com.reconcilitation.reconcilitation.model.interfaces.IScheduleReader#result(java.io.InputStream)
     */
    @Override
    public ScheduleReadResult result(InputStream csvData) {
        List<ScheduledTurn> turns = new ArrayList<>(); //list of turns
        List<RejectedRow> rejected = new ArrayList<>(); //list of rejected rows
        boolean isHeader = false; //header validator
        try {
            Reader reader = new InputStreamReader(csvData, StandardCharsets.UTF_8); //create a reader object from our CSV inputStream Data, coded with UTF 8 byte
            //traverse each record of csv file parsed with CSVFORMAT.Excel settings
            for (CSVRecord record : CSVFormat.EXCEL.parse(reader)) {
                if (!isHeader) {
                    isHeader = setHeader(record);
                } else if (!isEmpty(record)) {
                    //if record is not empty start to validate row information
                    readRow(record, turns, rejected);
                }
            }
            //return  ScheduleReadResult entity container
            return new ScheduleReadResult(turns, rejected);
        } catch (Exception e) {
        }
        throw new UnsupportedOperationException("CSV ERROR");
    }

    /**
     *  read row method to read CSV columns from Daily Schedule excel format
     * @param record csv record
     * @param turns current turns
     * @param rejected current rejections
     */
    private void readRow(CSVRecord record, List<ScheduledTurn> turns, List<RejectedRow> rejected) {
        long row = record.getRecordNumber();
        //validate if shift was completed
        if (!record.get(10).equalsIgnoreCase("SI")) {
            rejected.add(new RejectedRow(row, "not marked as completed (CUMPLIDO)"));
            return;
        }
        //nurse name column
        String nurse = record.get(7);
        //nurse name validation
        if (nurse.isEmpty()) {
            //create rejection if nurse name is not in place
            rejected.add(new RejectedRow(row, "nurse name is empty"));
            return;
        }
        // data column
        String rawDate = record.get(COL_DATE);
        LocalDate date;
        try {
            //parse date format
            date = parseDate(rawDate);
        } catch (DateTimeParseException e) {
            //create rejection for not readable date
            rejected.add(new RejectedRow(row, "date not readable: '" + rawDate + "'"));
            return;
        }
        // date has to be between 2000 and 2100 year
        if (date.getYear() < 2000 || date.getYear() > 2100) {
            rejected.add(new RejectedRow(row, "date out of range: '" + rawDate + "'"));
            return;
        }
        
        String first = record.get(5); //day shift
        String second = record.get(6);// night shift
        //if both are empty is considered as rejection
        if (!first.isEmpty() && !second.isEmpty()) {
            rejected.add(new RejectedRow(row, "both shift columns are filled: '" + first + "' and '" + second + "'"));
            return;
        }
        String shiftText = first.isEmpty() ? second : first;
        if (shiftText.isEmpty()) {
            rejected.add(new RejectedRow(row, "no shift"));
            return;
        }
        Shift shift;
        try {
            //shift has to be DAY or NIGHT.
            shift = Shift.parse(shiftText);
        } catch (IllegalArgumentException e) {
            rejected.add(new RejectedRow(row, "unsupported shift: '" + shiftText + "'"));
            return;
        }

        String rawValue = record.get(15); //shift cost
        String digits = rawValue.replaceAll("[^0-9]", ""); // any character is not a value, repllae for empty space regex pattern
        if (digits.isEmpty() || digits.length() > 12) {
            rejected.add(new RejectedRow(row, "turn value not readable: '" + rawValue + "'"));
            return;
        }
        //add to turns
        turns.add(new ScheduledTurn(date, shift, nurse, Long.parseLong(digits)));
    }

    /**
     * header setter 
     * @param record
     * @return header result
     */
    private static boolean setHeader(CSVRecord record) {
        //get record and replace any BOM chacracter with empty space
        return record.get(COL_DATE).replace("\uFEFF", "").trim().equalsIgnoreCase("FECHA");
    }

    /**
     * validate if values inside of record are empty
     * @param record
     * @return true or false
     */
    private static boolean isEmpty(CSVRecord record) {
        for (String value : record) {
            if (!value.isBlank()) {
                return false;
            }
        }
        return true;
    }
    /**
     * parse Date into passed format
     * @param raw
     * @return parsed date
     */
    private LocalDate parseDate(String raw) {
        return LocalDate.parse(raw, dateFormat);
    }

}
