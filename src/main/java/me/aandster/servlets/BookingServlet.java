package me.aandster.servlets;

import me.aandster.html.BookingPageHtmlGen;
import me.aandster.model.Booking;
import me.aandster.model.BookingManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class BookingServlet extends HttpServlet {

    /* Relative file path of save file for booking data */
    private final String PERSISTENCE_FILE_PATH = "/WEB-INF/booking_records.txt";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /* Validate and collect request parameters in local variables */

        String patientUsername = req.getParameter("username");
        if (patientUsername == null || patientUsername.isEmpty()) {
            resp.sendRedirect("login");
            return;
        }

        String selectedDoctor = req.getParameter("doctor");
        if (selectedDoctor == null || selectedDoctor.isEmpty()) {
            throw new ServletException("No parameter provided for doctor.");
        }

        String selectedDate = req.getParameter("date");
        if (selectedDate == null || selectedDate.isEmpty()) {
            throw new ServletException("No parameter provided for date.");
        }

        String selectedTime = req.getParameter("time");
        if (selectedTime == null || selectedTime.isEmpty()) {
            throw new ServletException("No parameter provided for time.");
        }

        /* Set-up model */
        String saveFileRealPath = getServletContext().getRealPath(PERSISTENCE_FILE_PATH);
        BookingManager bookingManager = new BookingManager(saveFileRealPath);

        /* Find if Booking details already exist */
        Booking existingBooking = bookingManager.lookupBooking(selectedDate, selectedTime, selectedDoctor);
        String patientEmail = (existingBooking == null) ? "" : existingBooking.getPatientEmailAddress();
        String patientPostalAddress = (existingBooking == null) ? "" : existingBooking.getPatientPostalAddress();
        String patientPhone = (existingBooking == null) ? "" : existingBooking.getPatientPhoneNumber();

        /* Check business rules re. max allowed bookings for patients */
        if (bookingManager.isBookingLimitReachedForCalendarWeek(selectedDate, patientUsername, bookingManager.lookupBooking(existingBooking))) {
            throw new ServletException("Patient has reached the limit for allowed bookings per calendar week.");
        }

        /* Check business rules re. seeing one doctor per calendar week */
        if (bookingManager.isBookedWithOtherDoctorsInCalendarWeek(selectedDate, patientUsername, selectedDoctor)) {
            throw new ServletException("Patient has bookings with other doctors in the same calendar week besides " + selectedDoctor + ".");
        }

        /* Set up HTML generator & transfer data*/
        BookingPageHtmlGen htmlgen = new BookingPageHtmlGen();
        htmlgen.patientName = patientUsername;
        htmlgen.doctorName = selectedDoctor;
        htmlgen.bookingDate = selectedDate;
        htmlgen.bookingTime = selectedTime;
        htmlgen.bookingEmail = (patientEmail == null) ? "" : patientEmail;
        htmlgen.bookingPostalAddress = (patientPostalAddress == null) ? "" : patientPostalAddress;
        htmlgen.bookingPhone = (patientPhone == null) ? "" : patientPhone;

        /* Return Response Body */
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println(htmlgen.page());

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /* Determine whether this is a Submit or Cancel/Delete operation */
        String operation = req.getParameter("operation");
        if (operation == null || operation.isEmpty()) {
            operation = "submit";
        } else if (!(operation.equals("submit") || operation.equals("delete"))) {
            throw new ServletException("Invalid parameter provided for operation: " + operation + ".");
        }

        /* Validate and collect request parameters in local variables */

        String username = req.getParameter("username");
        if (username == null || username.isEmpty()) {
            resp.sendRedirect("login");
            return;
        }

        String doctor = req.getParameter("doctor");
        if (doctor == null || doctor.isEmpty()) {
            throw new ServletException("No parameter provided for doctor.");
        }

        String bookingDate = req.getParameter("date");
        if (bookingDate == null || bookingDate.isEmpty()) {
            throw new ServletException("No parameter provided for date.");
        }

        String bookingTime = req.getParameter("time");
        if (bookingTime == null || bookingTime.isEmpty()) {
            throw new ServletException("No parameter provided for time.");
        }

        String emailAddress = req.getParameter("patient_email");
        if (!operation.equals("delete")) {
            if (emailAddress == null || emailAddress.isEmpty()) {
                throw new ServletException("No parameter provided for email.");
            }
            if (!emailAddress.contains("@")) {
                throw new ServletException("Email does not contain an @ symbol.");
            }
        }

        String postalAddress = req.getParameter("patient_postal");

        String phoneNo = req.getParameter("patient_phone");

        /* Create a Booking to represent request being deleted */
        Booking requestBooking = new Booking();
        requestBooking.setDate(bookingDate);
        requestBooking.setTime(bookingTime);
        requestBooking.setDoctorName(doctor);
        requestBooking.setPatientName(username);
        requestBooking.setPatientEmailAddress(emailAddress);
        requestBooking.setPatientPostalAddress(postalAddress);
        requestBooking.setPatientPhoneNumber(phoneNo);

        /* Get a Booking Manager */
        BookingManager bookingManager = new BookingManager(getServletContext().getRealPath(PERSISTENCE_FILE_PATH));

        if (operation.equals("delete")) {

            /* If delete operation, delete matching Booking from store */
            bookingManager.deleteBooking(requestBooking);

        } else {

            /* Otherwise, submit the Booking to be created or updated */
            bookingManager.submitBooking(requestBooking);
        }

        /* Redirect to Main/Calendar page when done */
        resp.sendRedirect("calendar?username=" + username + "&doctor=" + doctor /*+ "&week_start=" + bookingDate*/);
    }
}
