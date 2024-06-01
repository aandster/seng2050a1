package me.aandster.html;

@SuppressWarnings("FieldCanBeLocal")
public class LoginPageHtmlGen extends BaseHtmlGen {

    public LoginPageHtmlGen() {
        super("Login Page");
    }

    public String generate() {

        /* Goes inside <form...></form> */
        String loginFormContents = formTextField("username", "Username", "", "username", true)
                + formSubmitButton("Go")
                + formResetButton("Clear");

        /* The login form, ready to insert into <body> below */
        String loginForm = form("/c3259753_assignment1/calendar", "get", "return validateLoginForm();", loginFormContents);

        /* Goes inside <body></body> */
        String bodyContentsAfterHeader = paragraph("To proceed to the Booking Calendar, you must log in with your Username.")
                + paragraph("Please Note: Patients must use the same Username for all bookings. Usernames can be used by one patient only.")
                + lineBreak()
                + loginForm;

        return document(bodyContentsAfterHeader);
    }

}
