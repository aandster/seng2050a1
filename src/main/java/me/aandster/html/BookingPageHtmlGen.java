package me.aandster.html;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class BookingPageHtmlGen extends BaseHtmlGen {

    public String bookingDate = "";
    public String bookingTime = "";
    public String doctorName = "";
    public String patientName = "";
    public String bookingEmail = "";
    public String bookingPostalAddress = "";
    public String bookingPhone = "";

    public BookingPageHtmlGen() {
        super("Booking Page");
    }

    public String page() {

        /* Goes inside <body></body> */
        String bodyContentsAfterHeader = link("Log Out", "login")
                + lineBreak()
                + link("Back to Calendar", URLEncoder.encode("calendar?username=" + patientName + "&doctor=" + doctorName + "&week_start=" + bookingDate, StandardCharsets.UTF_8))
                + bodySection("Booking Details", unorderedList(bold("Doctor: ") + doctorName, bold("Date: ") + bookingDate, bold("Time: ") + bookingTime))
                + bodySection("Patient Details", patientDetailsForm())
                + bodySection("Cancel Appointment", cancelAppointmentButton());

        /* Complete HTML page */
        return document(bodyContentsAfterHeader);
    }

    private String patientDetailsForm() {
        String formInnerHtml = formHiddenField("operation", "submit", "details_operation") +
                formTextFieldDisabled("username", "Patient Name", patientName, "details_username2") +
                formHiddenField("username", patientName, "details_username") +
                formHiddenField("doctor", doctorName, "details_doctor") +
                formHiddenField("date", bookingDate, "details_date") +
                formHiddenField("time", bookingTime, "details_time") +
                formTextField("patient_email", "Email Address", bookingEmail, "details_patient_email", true) +
                formTextField("patient_postal", "Postal Address", bookingPostalAddress, "details_patient_postal",  false) +
                formTextField("patient_phone", "Phone Number", bookingPhone, "details_patient_phone", false) +
                formSubmitButton("Save Appointment") +
                formResetButton("Revert Changes");
        return form("booking", "post", "return validateBookingForm();", formInnerHtml);
    }

    private String cancelAppointmentButton() {
        String formInnerHtml = formHiddenField("operation", "delete", "cancel_operation")
                + formHiddenField("username", patientName, "cancel_username")
                + formHiddenField("doctor", doctorName, "cancel_doctor")
                + formHiddenField("date", bookingDate, "cancel_date")
                + formHiddenField("time", bookingTime, "cancel_time")
                + formSubmitButton("Cancel Appointment");
        return form("booking", "post", "return true;", formInnerHtml);
    }

}
