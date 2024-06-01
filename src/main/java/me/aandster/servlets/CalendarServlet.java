package me.aandster.servlets;

import me.aandster.html.CalendarPageHtmlGen;
import me.aandster.model.BookingManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

public class CalendarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /* Response Set-Up */
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        /* Collect request parameters */
        String clientUsername = req.getParameter("username");           // Patient's username entered prior
        String selectedDoctor = req.getParameter("doctor");             // Show schedule for this doctor
        String selectedWeek = req.getParameter("week_start");  // Determines week displayed on calendar

        /* Validate required request parameters (username) */
        if (clientUsername == null || clientUsername.isEmpty()) {
            resp.sendRedirect("login");     // Redirect to Login page if no username provided
            return;
        }

        /* Set-up model */
        String saveFilePath = getServletContext().getRealPath("/WEB-INF/booking_records.txt");
        BookingManager bookingManager = new BookingManager(saveFilePath);
        List<String> doctors = bookingManager.getDoctors();

        /* Validate doctor parameter or choose default */
        String defaultDoctor = doctors.get(0);
        if (selectedDoctor == null || selectedDoctor.isEmpty()) {
            selectedDoctor = defaultDoctor;
        } else {
            // See if doctor is listed, if not pick default
            boolean selectedDoctorValid = false;
            for (String dr : doctors) {
                if (dr.equals(selectedDoctor)) {
                    selectedDoctorValid = true;
                    break;
                }
            }
            if (!selectedDoctorValid) {
                selectedDoctor = defaultDoctor;
            }
        }

        /* Validate week parameter or choose default */
        SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String isoDateToday = LocalDate.now().toString();
        if (selectedWeek == null || selectedWeek.isEmpty()) {
            selectedWeek = isoDateToday;
        } else {
            // Try to parse the date provided
            boolean selectedWeekValid = false;
            try {
                isoDateFormat.parse(selectedWeek);
                selectedWeekValid = true;
            } catch (ParseException ignored) {
            }
            if (!selectedWeekValid) {
                selectedWeek = isoDateToday;
            }
        }

        /* HTML Generator Set-Up & Transfer Request Information */
        CalendarPageHtmlGen htmlgen = new CalendarPageHtmlGen();
        htmlgen.patientName = clientUsername;
        htmlgen.doctorChosen = selectedDoctor;
        htmlgen.weekViewStartDate = selectedWeek;
        htmlgen.listOfDoctors = doctors;
        htmlgen.bookingManager = bookingManager;

        /* Output page HTML */
        out.print(htmlgen.page());

        /* TESTING */
        //for (Booking b : bookingManager.getEveryBooking()) {
        //    out.print(b.getDate());
        //    out.print(" ~ ");
        //    out.print(b.getTime());
        //    out.print(" ~ ");
        //    out.print(b.getDoctor());
        //    out.print(" ~ ");
        //    out.print(b.getPatientName());
        //    out.print(" ~ ");
        //    out.print(b.getPatientEmailAddress());
        //    out.print(" ~ ");
        //    out.print(b.getPatientPostalAddress());
        //    out.print(" ~ ");
        //    out.print(b.getPatientPhoneNumber());
        //    out.println(" ~~~~~~~~~~~~ <br>");
        //}
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
