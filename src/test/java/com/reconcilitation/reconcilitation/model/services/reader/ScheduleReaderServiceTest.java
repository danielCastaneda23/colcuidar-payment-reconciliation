package com.reconcilitation.reconcilitation.model.services.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.reconcilitation.reconcilitation.model.ScheduleReadResult;
import com.reconcilitation.reconcilitation.model.ScheduledTurn;
import com.reconcilitation.reconcilitation.model.enums.Shift;

class ScheduleReaderServiceTest {

    private static final String TITLE = "PRODUCTIVIDAD OCTUBRE-2026";
    private static final String HEADER = "FECHA,,,,,TURNO,,AUX,,,CUMPLIDO,,,,,VALOR";

    private final ScheduleReaderService service = new ScheduleReaderService();

    // Creation of csv file with 16 columns
    private static String row(String date, String first, String second,
            String nurse, String done, String value) {
        String[] c = new String[16];
        Arrays.fill(c, "");
        c[0] = date;
        c[5] = first;
        c[6] = second;
        c[7] = nurse;
        c[10] = done;
        c[15] = value;
        return String.join(",", c);
    }

    private static String validRow() {
        return row("1/10/26", "DIA", "", "PEREZ ANA", "SI", "95000");
    }

    private ScheduleReadResult readCsv(String csv) {
        return service.result(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
    }

    // schdule result reader
    private ScheduleReadResult read(String... dataRows) {
        String csv = TITLE + "\n" + HEADER + "\n" + String.join("\n", dataRows) + "\n";
        return readCsv(csv);
    }

    // reads one bad row and checks it was rejected, returning the reason.
    private String rejectionValidation(String badRow) {
        ScheduleReadResult result = read(badRow);
        assertTrue(result.turns().isEmpty());
        assertEquals(1, result.rejectedRows().size());
        return result.rejectedRows().get(0).reason();
    }

    @Test
    void readsDayShiftFromTheFirstShiftColumn() {
        ScheduleReadResult result = read(validRow());

        assertEquals(1, result.turns().size());
        assertEquals(new ScheduledTurn(LocalDate.of(2026, 10, 1), Shift.DAY, "PEREZ ANA", 95_000),
                result.turns().get(0));
        assertTrue(result.rejectedRows().isEmpty());
    }

    @Test
    void readsNightShiftFromTheSecondShiftColumn() {
        ScheduleReadResult result = read(row("2/10/26", "", "NOCHE", "GOMEZ LUIS", "SI", "92000"));

        assertEquals(new ScheduledTurn(LocalDate.of(2026, 10, 2), Shift.NIGHT, "GOMEZ LUIS", 92_000),
                result.turns().get(0));
    }

    @Test
    void acceptsFourDigitYears() {
        ScheduleReadResult result = read(row("5/10/2026", "DIA", "", "PEREZ ANA", "SI", "95000"));

        assertEquals(LocalDate.of(2026, 10, 5), result.turns().get(0).date());
    }

    @Test
    void readsValuesWithCurrencySymbolAndSeparators() {
        ScheduleReadResult result = read(row("1/10/26", "DIA", "", "PEREZ ANA", "SI", "\"$95,000\""));

        assertEquals(95_000, result.turns().get(0).value());
    }

    @Test
    void toleratesAByteOrderMarkBeforeTheHeader() {
        String csv = TITLE + "\n\uFEFF" + HEADER + "\n" + validRow() + "\n";

        assertEquals(1, readCsv(csv).turns().size());
    }

    @Test
    void usesTheDateFormatPassedToTheConstructor() {
        ScheduleReaderService isoService = new ScheduleReaderService(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String csv = HEADER + "\n" + row("2026-10-05", "DIA", "", "PEREZ ANA", "SI", "95000") + "\n";

        ScheduleReadResult result = isoService.result(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        assertEquals(LocalDate.of(2026, 10, 5), result.turns().get(0).date());
    }

    @Test
    void rejectsRowNotMarkedAsCompleted() {
        assertTrue(rejectionValidation(row("1/10/26", "DIA", "", "PEREZ ANA", "NO", "95000"))
                .contains("not marked as completed"));
    }

    @Test
    void rejectsRowWithEmptyNurseName() {
        assertTrue(rejectionValidation(row("1/10/26", "DIA", "", "", "SI", "95000"))
                .contains("nurse name is empty"));
    }

    @Test
    void rejectsDateThatCannotBeRead() {
        assertTrue(rejectionValidation(row("5//10/2026", "DIA", "", "PEREZ ANA", "SI", "95000"))
                .contains("date not readable"));
    }

    @Test
    void rejectsDateOutsideTheValidYearRange() {
        assertTrue(rejectionValidation(row("1/10/0206", "DIA", "", "PEREZ ANA", "SI", "95000"))
                .contains("date out of range"));
    }

    @Test
    void rejectsRowWithBothShiftColumnsFilled() {
        assertTrue(rejectionValidation(row("1/10/26", "DIA", "NOCHE", "PEREZ ANA", "SI", "95000"))
                .contains("both shift columns"));
    }

    @Test
    void rejectsRowWithNoShift() {
        assertTrue(rejectionValidation(row("1/10/26", "", "", "PEREZ ANA", "SI", "95000"))
                .contains("no shift"));
    }

    @Test
    void rejectsUnsupportedShiftSuchAsTardeNoche() {
        assertTrue(rejectionValidation(row("1/10/26", "TARDE-NOCHE", "", "PEREZ ANA", "SI", "225000"))
                .contains("unsupported shift"));
    }

    @Test
    void rejectsValueWithoutDigits() {
        assertTrue(rejectionValidation(row("1/10/26", "DIA", "", "PEREZ ANA", "SI", "LL"))
                .contains("turn value not readable"));
    }

    @Test
    void keepsValidRowsAndReportsBadOnesInTheSameFile() {
        ScheduleReadResult result = read(
                validRow(),
                row("1/10/26", "DIA", "", "PEREZ ANA", "NO", "95000"),
                row("2/10/26", "", "NOCHE", "GOMEZ LUIS", "SI", "92000"));

        assertEquals(2, result.turns().size());
        assertEquals(1, result.rejectedRows().size());
    }

    @Test
    void ignoresBlankRowsWithoutReportingThem() {
        ScheduleReadResult result = read(
                validRow(),
                row("", "", "", "", "", ""),
                "");

        assertEquals(1, result.turns().size());
        assertTrue(result.rejectedRows().isEmpty());
    }

    @Test
    void reportsTheCsvRecordNumberOfRejectedRows() {
        ScheduleReadResult result = read(row("1/10/26", "DIA", "", "PEREZ ANA", "NO", "95000"));

        assertEquals(3, result.rejectedRows().get(0).rowNumber());
    }

    @Test
    void returnsAnEmptyResultWhenThereIsNoHeaderRow() {
        ScheduleReadResult result = readCsv("not,a,schedule\n1,2,3\n");

        assertTrue(result.turns().isEmpty());
        assertTrue(result.rejectedRows().isEmpty());
    }

    @Test
    void failsWithUnsupportedOperationWhenARowHasTooFewColumns() {
        assertThrows(UnsupportedOperationException.class, () -> read("1/10/26,DIA"));
    }
}
