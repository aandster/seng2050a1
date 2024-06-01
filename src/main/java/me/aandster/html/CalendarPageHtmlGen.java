package me.aandster.html;

import me.aandster.model.Booking;
import me.aandster.model.BookingManager;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("FieldCanBeLocal")
public class CalendarPageHtmlGen extends BaseHtmlGen {

    public final int DAYS_TO_VIEW = 7;
    public BookingManager bookingManager;
    public String patientName = "";
    public String doctorChosen = "";
    public String weekViewStartDate = "";
    public List<String> listOfDoctors;

    public CalendarPageHtmlGen() {
        super("Booking Lookup");
    }

    public String page() throws IOException {

        /* Goes inside <body></body> */
        String bodyContentsAfterHeader = link("Log Out", "login")
                //+ paragraph("Welcome back, " + PATIENT_USERNAME + ".")
                + bodySection("Terms and Conditions", unorderedList("Each patient can book a maximum of three appointments in a week.", "All bookings for a patient in a calendar week must be with the same doctor."))
                + bodySection("Selected Parameters", unorderedList(bold("Patient: ") + patientName, bold("Doctor: ") + doctorChosen, bold("Selected Date: ") + weekViewStartDate))
                + bodySection("Lookup Calendar", viewPickerForm() + lineBreak() + relativeNavigationForms() + lineBreak() + lineBreak() + generateCalendar());

        /* Complete HTML page */
        return document(bodyContentsAfterHeader);
    }

    private String viewPickerForm() {

        /* Generate InnerHtml for <form> */
        String formInnerHtml = formHiddenField("username", patientName, "picker_username") +
                "\n" +
                formDropdownBox("doctor", "Change Doctor", listOfDoctors, doctorChosen, "picker_doctor") +
                "\n" +
                formDatePicker("week_start", "Go to Date", weekViewStartDate) +
                "\n" +
                formSubmitButton("Go") +
                "\n";

        /* Generate completed <form> */
        return form("calendar", "get", "return true;", formInnerHtml);
    }

    private String relativeNavigationForms() {
        LocalDate viewStartDate = LocalDate.parse(weekViewStartDate);
        LocalDate sevenDaysBefore = viewStartDate.minusDays(7);
        LocalDate sevenDaysAfter = viewStartDate.plusDays(7);
        LocalDate oneDaysBefore = viewStartDate.minusDays(1);
        LocalDate oneDaysAfter = viewStartDate.plusDays(1);
        LocalDate dateToday = LocalDate.now();

        String commonFormHtml = formHiddenField("username", patientName, null)
                + formHiddenField("doctor", doctorChosen, null);

        return "<div class=relative_navigation>"
                + form("calendar", "get", "return true;", commonFormHtml + formHiddenField("week_start", sevenDaysBefore.toString(), null) + formSubmitButton("< Back 7 Days"))
                + form("calendar", "get", "return true;", commonFormHtml + formHiddenField("week_start", oneDaysBefore.toString(), null) + formSubmitButton("< Back 1 Day"))
                + form("calendar", "get", "return true;", commonFormHtml + formHiddenField("week_start", dateToday.toString(), null) + formSubmitButton("Today"))
                + form("calendar", "get", "return true;", commonFormHtml + formHiddenField("week_start", oneDaysAfter.toString(), null) + formSubmitButton("Forward 1 Day >"))
                + form("calendar", "get", "return true;", commonFormHtml + formHiddenField("week_start", sevenDaysAfter.toString(), null) + formSubmitButton("Forward 7 Days >"))
                +"</div>";
    }

    private String generateCalendar() throws IOException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        LocalDate viewStartDate = LocalDate.parse(weekViewStartDate);

        /* Enumerate the days we want to display on the calendar */
        String[] lookupDates = new String[DAYS_TO_VIEW];
        for (int i = 0; i < DAYS_TO_VIEW; i++) {
            lookupDates[i] = viewStartDate.plusDays(i).toString();
        }

        /*  */
        String[] lookupTimes = bookingManager.getAllowedBookingTimes();

        /*  */
        StringBuilder firstRowInnerHtml = new StringBuilder(tableHeaderCell(""));
        for (String lookupDate : lookupDates) {
            String dayOfWeek = LocalDate.parse(lookupDate).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + lineBreak();
            firstRowInnerHtml.append(tableHeaderCell(dayOfWeek + lookupDate));
        }

        /*  */
        StringBuilder tableInnerHtml = new StringBuilder(tableRow(firstRowInnerHtml.toString()));
        for (String lookupTime : lookupTimes) {
            StringBuilder rowInnerHtml = new StringBuilder();
            for (int k = 0; k < DAYS_TO_VIEW + 1; k++) {
                if (k == 0) {
                    rowInnerHtml.append(tableHeaderCell(lookupTime));
                } else {
                    String lookupDate = lookupDates[k - 1];
                    Booking queryResult = bookingManager.lookupBooking(lookupDate, lookupTime, doctorChosen);
                    rowInnerHtml.append(generateCalendarCell(queryResult, lookupDate, lookupTime));
                }
            }
            tableInnerHtml.append(tableRow(rowInnerHtml.toString()));
        }

        /*  */
        return table(tableInnerHtml.toString());
    }

    private String generateCalendarCell(Booking booking, String lookupDate, String lookupTime) throws IOException {

        if (booking == null || booking.getPatientName().isEmpty()) {
            if (bookingManager.isBookingLimitReachedForCalendarWeek(lookupDate, patientName, booking)
                    || bookingManager.isBookedWithOtherDoctorsInCalendarWeek(lookupDate, patientName, doctorChosen)) {
                /* Available appointment BUT user has reached limit */
                String cellInnerHtml = "Restricted";
                return tableDataCell(cellInnerHtml, "restricted");
            } else {
                /* Available appointment */
                String cellInnerHtml = link("Available", urlForBookingPage(lookupDate, lookupTime, doctorChosen));
                return tableDataCell(cellInnerHtml, "available");
            }
        } else if (booking.getPatientName().equals(patientName)) {
            /* Confirmed appointment */
            String cellInnerHtml = link("Confirmed", urlForBookingPage(lookupDate, lookupTime, doctorChosen));
            return tableDataCell(cellInnerHtml, "confirmed");
        } else {
            /* Unavailable Appointment */
            String cellInnerHtml = "Unavailable";
            return tableDataCell(cellInnerHtml, "unavailable");
        }
    }

    private String urlForBookingPage(String date, String time, String doctor) {
        java.nio.charset.Charset charSet = StandardCharsets.UTF_8;
        return "/c3259753_assignment1/booking?username=" + URLEncoder.encode(patientName, charSet)
                + "&doctor=" + URLEncoder.encode(doctor, charSet)
                + "&date=" + URLEncoder.encode(date, charSet)
                + "&time=" + URLEncoder.encode(time, charSet);
    }

    private String urlForCalendarPage(String newWeekStartDate) {
        java.nio.charset.Charset charset = StandardCharsets.UTF_8;

        String outUrl = "/c3259753_assignment1/calendar?username=" + URLEncoder.encode(patientName, charset);
        if (doctorChosen != null && !doctorChosen.isEmpty()) outUrl += "&doctor=" + URLEncoder.encode(doctorChosen, charset);
        outUrl+= "&week_start=" + URLEncoder.encode(weekViewStartDate, charset);

        return outUrl;
    }
}
