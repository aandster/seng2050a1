Student Name: Jordan Maddock
Student Number: 3259753
Course: SENG2050
Date: 27/03/2024

 # ASSIGNMENT 1

 ## HOW TO RUN

    1. Go to .../c3259753_assignment/login

    2. On /login page, Enter your name (patient's name)

    3. On /calendar page, view the weekly calendar and adjust view options until you select a booking timeslot.

    4. On /booking page, view the chosen appointment details, and:

        A. Enter your details, and click submit to update/create the appointment.

        B. Click the Cancel button to delete the booking.

    5. Click Log Out to return to /login page.



 ## DESIGN AND IMPLEMENTATION NOTES

 ### Timeslot states

    Internally, bookings (doctor x day x time) are either available to take or not.

    The calendar view adds two additional states that affect view logic depending on who is viewing the information.

    Timeslot codes are as follows:

        - Available: not booked
        - Unavailable: booked by another user
        - Confirmed: booked by you
        - Restricted: can be booked by other users, cannot be booked by you because of business rules (i.e. max 3 per week, only one doctor, ...)


 ### How is booking data stored?

    Booking data is stored in a text file at /booking_records.txt.

    Format is similar to CSV, with fields delimited with | symbols, and records delimited by new lines.

    The class BookingFileConnector class handles file IO operations.


 ### What is the application structure, i.e. relationships between objects

    The HttpServlets each use a respective concrete implementation of a BaseHtmlGen.

    The HttpServlets also use a BookingManager that handles fetching/persisting booking data while enforcing business rules.

    The BookingManager uses a BookingFileConnector to handle file IO operations for booking data.

    All classes use Bookings to represent individual bookings (or timeslots) that can be booked by a patient for a doctor.


 ### Package & Class Structure

    - me.aandster

        - html

            - BaseHtmlGen:          Abstract super class for all others in this package.

            - BookingPageHtmlGen:   Generates HTML for /booking page.

            - CalendarPageHtmlGen:  Generates HTML for /calendar (Main) page.

            - LoginPageHtmlGen:     Generates HTML for /login page.

        - model

            - Booking:              Represents a single booking or timeslot that has been or can be reserved by a patient for a doctor.

            - BookingManager:       Handles business logic and data, persists and provides booking data.

        - persistence

            - BookingFileConnector: Handles File IO for Booking data persistence

        - servlets

            - BookingServlet:       HttpServlet for /booking page.

            - CalendarServlet:      HttpServlet for /calendar (Main) page.

            - LoginServlet:         HttpServlet for /login page.


