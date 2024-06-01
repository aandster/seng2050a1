package me.aandster.persistence;

import me.aandster.model.Booking;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BookingFileConnector {

    private final String FILE_PATH;

    public BookingFileConnector(String path) {
        if (path == null) throw new NullPointerException();
        this.FILE_PATH = path;
    }

    /**
     * Reads data from file
     * @return List of Bookings from file
     * @throws IOException If File IO errors.
     */
    public synchronized List<Booking> fetchFromFile() throws IOException {

        /* Read file into memory, or create a blank file if it does not exist. */
        List<String> fileLines = null;
        try {
            fileLines = Files.readAllLines(Path.of(FILE_PATH));
        } catch (NoSuchFileException e) {
            instantiateFile();
            fileLines = Files.readAllLines(Path.of(FILE_PATH));
        }

        /* Parse text from file into a List of Booking objects */
        List<Booking> instantiatedBookingObjects = new ArrayList<>();
        for (String lineContents : fileLines) {
            
            /* Ignore blank lines */
            if (lineContents.isEmpty()) continue;

            /* Split the line/record by field/column/delimiter */
            Booking newBookingObject = parseBookingFileRecord(lineContents);

            /* Add to the list of instantiated Booking objects */
            instantiatedBookingObjects.add(newBookingObject);
        }
        return instantiatedBookingObjects;
    }

    /**
     * Parse one record/line of raw file text
     * @param lineContents text from one line/record of file
     * @return Booking object representing record in file
     * @throws IOException If File Io errors.
     */
    private static Booking parseBookingFileRecord(String lineContents) throws IOException {
        String[] splitLine = lineContents.split("\\|");

        /* Check that the column width is correct; throw exception if not. */
        int FILE_RECORD_FIELDS_EXPECTED = 7;
        if (splitLine.length != FILE_RECORD_FIELDS_EXPECTED) throw new IOException("Column width incorrect. Expected " + FILE_RECORD_FIELDS_EXPECTED + " but got " + splitLine.length);

        /* Create the new Booking object */
        Booking newBookingObject = new Booking();
        newBookingObject.setDate(splitLine[0]);
        newBookingObject.setTime(splitLine[1]);
        newBookingObject.setDoctorName(splitLine[2]);
        newBookingObject.setPatientName(splitLine[3]);
        newBookingObject.setPatientEmailAddress(splitLine[4]);
        newBookingObject.setPatientPostalAddress(splitLine[5].trim());
        newBookingObject.setPatientPhoneNumber(splitLine[6].trim());
        return newBookingObject;
    }

    /**
     * Creates a blank file, or clears data from file if it already exists.
     * @throws IOException If File IO errors.
     */
    private synchronized void instantiateFile() throws IOException {
        Files.createFile(Path.of(FILE_PATH));
    }

    /**
     * Overwrites file with contents of list provided
     * @param bookingList List of Booking objects to be saved in file
     * @return List of Booking objects that are read from file after write operation is completed.
     * @throws IOException If File IO errors.
     */
    @SuppressWarnings("UnusedReturnValue")
    public synchronized List<Booking> overwriteFile(List<Booking> bookingList) throws IOException {
        try (PrintWriter printWriter = new PrintWriter(FILE_PATH, StandardCharsets.UTF_8)) {
            for (Booking bookingObject : bookingList) {
                if (bookingObject == null) continue;
                char FILE_FIELD_DELIMITER = '|';
                String lineText = bookingObject.getDate()
                        + FILE_FIELD_DELIMITER + bookingObject.getTime().replace("|", "_")
                        + FILE_FIELD_DELIMITER + bookingObject.getDoctor().replace("|", "_")
                        + FILE_FIELD_DELIMITER + bookingObject.getPatientName().replace("|", "_")
                        + FILE_FIELD_DELIMITER + bookingObject.getPatientEmailAddress().replace("|", "_")
                        + FILE_FIELD_DELIMITER;
                lineText += (bookingObject.getPatientPostalAddress().isEmpty()) ? " " : bookingObject.getPatientPostalAddress().replace("|", "_");
                lineText += FILE_FIELD_DELIMITER;
                lineText += (bookingObject.getPatientPhoneNumber().isEmpty()) ? " " : bookingObject.getPatientPhoneNumber().replace("|", "_");

                printWriter.println(lineText);  // \n added here
            }
        }
        return fetchFromFile();
    }

}
