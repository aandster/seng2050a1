package me.aandster.model;

import me.aandster.persistence.BookingFileConnector;

import java.awt.print.Book;
import java.io.IOException;
import java.security.AccessControlException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class BookingManager {

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean ENFORCE_ONE_DOCTOR_PER_PATIENT_PER_CALENDAR_WEEK = true;
    private final int MAX_BOOKINGS_PER_PATIENT_PER_CALENDAR_WEEK = 3;
    private final String[] ALLOWED_BOOKING_TIMES = new String[]{"06:00", "06:30", "07:00", "07:30", "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30"};
    private final String[] DOCTORS = new String[]{"Doctor Alice", "Doctor Bob", "Doctor Carel"};

    /* Handles file IO */
    private final BookingFileConnector bookingFileConnector;

    /**
     * Accepts path to save file.
     * @param bookingFilePath Absolute path to save file
     */
    public BookingManager(String bookingFilePath) {
        this.bookingFileConnector = new BookingFileConnector(bookingFilePath);
    }

    /**
     * Finds an existing booking and returns a representative Booking, or returns null if no matches found.
     * @param date Appointment date to search on
     * @param time Appointment time to search on
     * @param doctor Doctor name to search on
     * @return null if no booking found, otherwise a Booking object satisfying the search query.
     * @throws IOException If File IO has errors.
     * @throws IllegalStateException If duplicate records found in save file.
     */
    public synchronized Booking lookupBooking(String date, String time, String doctor) throws IOException {
        List<Booking> allBookings = bookingFileConnector.fetchFromFile();
        Booking foundBooking = null;
        for (Booking b : allBookings) {
            boolean matchFound = b.getDate().equals(date) && b.getTime().equals(time) && b.getDoctor().equals(doctor);
            if (matchFound) {
                if (foundBooking != null)
                    throw new IllegalStateException("Duplicate records found in file. Query was: " + date + ", " + time + ", " + doctor + ".");
                foundBooking = b;
            }
        }
        return foundBooking;
    }

    /**
     * Finds an existing booking and returns a representative Booking, or returns null if no matches found.
     * @param booking Booking object used to search on to find a matching existing Booking from save file.
     * @return null if no booking found, otherwise a Booking object satisfying the search query.
     * @throws IOException If File IO has errors.
     * @throws IllegalStateException If duplicate records found in save file.
     */
    public synchronized Booking lookupBooking(Booking booking) throws IOException {
        List<Booking> allBookings = bookingFileConnector.fetchFromFile();
        Booking foundBooking = null;
        if (booking == null) return null;
        for (Booking b : allBookings) {
            boolean matchFound = b.getDate().equals(booking.getDate())
                    && b.getTime().equals(booking.getTime())
                    && b.getDoctor().equals(booking.getDoctor());
            if (matchFound) {
                if (foundBooking != null)
                    throw new IllegalStateException("Duplicate records found in file. Query was: " + booking.getDate() + ", " + booking.getTime() + ", " + booking.getDoctor() + ".");
                foundBooking = b;
            }
        }
        return foundBooking;
    }

    /**
     * @return Bookable doctors
     */
    public synchronized List<String> getDoctors() {
        return Arrays.asList(DOCTORS);
    }

    /**
     * Returns a list of every booking in the save file.
     * @return List of all bookings
     * @throws IOException If File IO errors
     */
    public synchronized List<Booking> getEveryBooking() throws IOException {
        return bookingFileConnector.fetchFromFile();
    }

    /**
     * Deletes a booking from the file that matches the provided Booking object.
     * @param booking Object with details matching Booking to be deleted.
     * @throws IOException if File IO errors
     */
    public synchronized void deleteBooking(Booking booking) throws IOException {

        /* Check the database to find the matching Booking */
        List<Booking> inMemoryBookings = getEveryBooking();
        Booking matchingExistingBooking = null;
        for (Booking b : inMemoryBookings) {
            if (b.equals(booking)) {
                if (matchingExistingBooking != null) throw new IllegalStateException("Duplicate records found in file. Query was: " + booking.getDate() + ", " + booking.getTime() + ", " + booking.getDoctor() + ".");
                matchingExistingBooking = b;
            }
        }

        /* If the requested booking could not be found */
        if (matchingExistingBooking == null) {
            throw new InternalError("Requested booking could ot be found: " + booking.toString() + "." );
        }

        /* Check that current user owns the appointment */
        if (!booking.getPatientName().equals(matchingExistingBooking.getPatientName())) {
            throw new InternalError("Current user does not own the Booking to be deleted.");
        }

        /* Remove Booking from List in memory */
        if (!inMemoryBookings.remove(matchingExistingBooking)) {
            throw new InternalError("Could not complete deletion of Booking: " + booking.toString());
        }

        /* Overwrite modified List to txt file */
        bookingFileConnector.overwriteFile(inMemoryBookings);
    }

    /**
     * Commits a Booking reservation, which will create a new booking if it does not exist, or update existing booking if user owns the booking.
     * @param submittedBooking Booking representing booking we want to reserve
     * @throws IOException If File IO errors
     */
    public synchronized void submitBooking(Booking submittedBooking) throws IOException {

        /* Search for existing bookings */
        Booking existingBooking = lookupBooking(submittedBooking);

        /* Determine if booking exists already in same time for doctor */
        boolean isPreExistingRecordOnFile = existingBooking != null;

        /* If the appointment exists, and does not belong to the current user: */
        if (isPreExistingRecordOnFile && !submittedBooking.getPatientName().equals(existingBooking.getPatientName())) {
            throw new AccessControlException("The requested timeslot has already been reserved by another user.");
        }

        /* Does the field data satisfy business data validation requirements? */
        List<String> errorList = new LinkedList<>();

        /* No digits in patient's name */
        String newBookingPatientName = submittedBooking.getPatientName();
        if (newBookingPatientName == null || newBookingPatientName.isEmpty()) {
            errorList.add("No value provided for patient name.");
        } else if (Pattern.compile("[0-9]").matcher(newBookingPatientName).find()) {
            errorList.add("There are digits in the patient name.");
        }

        /* Email must have @ in it*/
        String nbEmail = submittedBooking.getPatientEmailAddress();
        if (nbEmail == null || nbEmail.isEmpty()) {
            errorList.add("No email address provided.");
        } else if (!nbEmail.contains("@")) {
            errorList.add("Email address does nto contain an @ symbol.");
        }

        /* Validate date */
        String newBookingDate = submittedBooking.getDate();
        try {
            //noinspection ResultOfMethodCallIgnored
            LocalDate.parse(newBookingDate);
            newBookingDate = submittedBooking.getDate();
        } catch (DateTimeParseException e) {
            errorList.add("Invalid date provided.");
        }

        /* Validate time */
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String newBookingTime = submittedBooking.getTime();
        if (newBookingTime == null || newBookingTime.isEmpty()) {
            errorList.add("No time provided for appointment.");
        }
        try {
            timeFormat.parse(newBookingTime);
        } catch (ParseException e) {
            errorList.add("Invalid time provided: " + newBookingTime + ".");
        }

        /* Validate Doctor */
        String newBookingDoctor = submittedBooking.getDoctor();
        if (newBookingDoctor == null || newBookingDoctor.isEmpty()) {
            errorList.add("No value provided for doctor name.");
        }

        /* Exit now */
        if (!errorList.isEmpty()) throw new AccessControlException(errorList.toString());

        /* Validate business rules:
            1.  Will the user exceed the limit of three appointments per calendar week?
            2.  Does the user already have a booking with another doctor in that calendar week? */

        /* Check patient has not exceeded max allowable appointments per calendar week */
        if (isBookingLimitReachedForCalendarWeek(submittedBooking.getDate(), newBookingPatientName, submittedBooking)) {
            errorList.add("Patient currently has the maximum allowable number of bookings per calendar week. The max is " + MAX_BOOKINGS_PER_PATIENT_PER_CALENDAR_WEEK + ".");
        }

        /* Check patient does not have booking with different doctor in same calendar week */
        if (isBookedWithOtherDoctorsInCalendarWeek(LocalDate.parse(submittedBooking.getDate()), newBookingPatientName, submittedBooking.getDoctor())) {
            errorList.add("Patient has existing bookings with other doctors in the same calendar week.");
        }

        if (!errorList.isEmpty()) throw new AccessControlException(errorList.toString());

        /* Get all existing bookings */
        List<Booking> prodBookingList = getEveryBooking();

        /* Remove existing booking from list */
        Booking deadBookingObject = null;
        if (existingBooking != null) {
            for (Booking b : prodBookingList) {
                if (b.getDate().equals(newBookingDate.toString()) && b.getTime().equals(newBookingTime) && b.getDoctor().equals(newBookingDoctor)) {
                    prodBookingList.remove(b);
                    break;
                }
            }
        }

        /* Insert new booking into list */
        prodBookingList.add(submittedBooking);

        /* Save modified list to file */
        bookingFileConnector.overwriteFile(prodBookingList);
    }

    /**
     * Check if a patient has or will exceed their booking limit (of 3) per calendar week.
     * @param bookingDate The date falling in the respective calendar week
     * @param patientName The name of the patient
     * @param transactionBooking Representation of the booking for the current transaction (because if updating an existing booking, it will not trigger the condition, because after the transaction the same amount of bookings will be there).
     * @return True if the user can create another appointment without exceeding their limit
     * @throws IOException If File IO errors
     */
    public boolean isBookingLimitReachedForCalendarWeek(String bookingDate, String patientName, Booking transactionBooking) throws IOException {
        LocalDate bookingDateObj = LocalDate.parse(bookingDate);
        LocalDate correspondingSundayBeforeDate = bookingDateObj.minusDays((bookingDateObj.getDayOfWeek().getValue() - 1 + 1));
        LocalDate correspondingFollowingMonday = correspondingSundayBeforeDate.plusDays(8);
        int existingBookingsInSameCalendarWeek = 0;
        for (Booking b : getEveryBooking()) {
            LocalDate bLocalDate = LocalDate.parse(b.getDate());
            if (patientName.equals(b.getPatientName())
                    && bLocalDate.isAfter(correspondingSundayBeforeDate)
                    && bLocalDate.isBefore(correspondingFollowingMonday)
                    && !b.equals(transactionBooking)) {
                existingBookingsInSameCalendarWeek++;
            }
        }
        return existingBookingsInSameCalendarWeek >= MAX_BOOKINGS_PER_PATIENT_PER_CALENDAR_WEEK;
    }

    public boolean isBookedWithOtherDoctorsInCalendarWeek(LocalDate bookingDate, String patientName, String doctorName) throws IOException {
        return isBookedWithOtherDoctorsInCalendarWeek(bookingDate.toString(), patientName, doctorName);
    }

    /**
     * Checks if user has booked with a doctor other than the one specified in the calendar week that the specified booking date belongs to.
     * @param bookingDateStr Booking date that belongs to the calendar week in question
     * @param patientName Patient in question
     * @param doctorName Doctor patient would like to enquire about
     * @return True if date is bookable based on business logic
     * @throws IOException If File IO errors.
     */
    public boolean isBookedWithOtherDoctorsInCalendarWeek(String bookingDateStr, String patientName, String doctorName) throws IOException {
        LocalDate bookingDate = LocalDate.parse(bookingDateStr);
        LocalDate correspondingSundayBeforeDate = bookingDate.minusDays((bookingDate.getDayOfWeek().getValue() - 1 + 1));
        LocalDate correspondingFollowingMonday = correspondingSundayBeforeDate.plusDays(8);
        for (Booking b : getEveryBooking()) {
            LocalDate bLocalDate = LocalDate.parse(b.getDate());
            if (ENFORCE_ONE_DOCTOR_PER_PATIENT_PER_CALENDAR_WEEK
                    && patientName.equals(b.getPatientName())
                    && bLocalDate.isAfter(correspondingSundayBeforeDate)
                    && bLocalDate.isBefore(correspondingFollowingMonday)) {
                if (!doctorName.equals(b.getDoctor())) return true;
            }
        }
        return false;
    }

    public String[] getAllowedBookingTimes() {
        return ALLOWED_BOOKING_TIMES;
    }
}
