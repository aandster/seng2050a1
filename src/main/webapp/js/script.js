/* Jordan Maddock c3259753 SENG2050 Assignment 1 2024 */

function validateLoginForm() {
    let resultStatus = true;
    let username = document.getElementById("username");
    if (/\d/.test(username.value)) {
        alert("Username cannot contain digits");
        return false;
    }
    return true;
}

function validateBookingForm() {
    let providedEmail = document.getElementById("details_patient_email")
    if (!/@/.test(providedEmail.value)) {
        alert("Email address must contain an @ symbol.");
        return false;
    }
    return true;
}