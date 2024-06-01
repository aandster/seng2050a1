package me.aandster.model;

public class Booking {

    private String date = null;
    private String time = null;
    private String doctorName = null;
    private String patientName = "";
    private String patientPhoneNumber = "";
    private String patientPostalAddress = "";
    private String patientEmailAddress = "";

    public String getPatientPhoneNumber() {
        return patientPhoneNumber;
    }

    public void setPatientPhoneNumber(String patientPhoneNumber) {
        this.patientPhoneNumber = patientPhoneNumber;
    }

    public String getPatientPostalAddress() {
        return patientPostalAddress;
    }

    public void setPatientPostalAddress(String patientPostalAddress) {
        this.patientPostalAddress = patientPostalAddress;
    }

    public String getPatientEmailAddress() {
        return patientEmailAddress;
    }

    public void setPatientEmailAddress(String patientEmailAddress) {
        this.patientEmailAddress = patientEmailAddress;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDoctor() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    /* Returns true if the following attributes are equal: date, time, doctor */
    public boolean equals(Booking other) {
        return other != null
                && this.getDoctor().equals(other.getDoctor())
                && this.getDate().equals(other.getDate())
                && this.getTime().equals(other.getTime());
    }

    public String toString() {
        return "Booking: [" + getDate() + ", " + getTime() + ", " + getDoctor() + "]";
    }
}
