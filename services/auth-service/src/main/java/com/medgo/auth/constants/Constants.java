package com.medgo.auth.constants;

public final class Constants {


    private Constants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String PASSWORD_NOT_ALLOWED = "Password not allowed";
    public static final String USER_ALREADY_REGISTERED = "User already registered";
    public static final String USER_ALREADY_REGISTERED_MEMBER = "User already registered as member";
    public static final String EMAIL_NOT_BLANK = "Email must not be empty.";
    public static final String INVALID_EMAIL = "Invalid email format.";
    public static final String EMAIL_MAX_LENGTH = "Email exceeds maximum length of 100 characters.";
    public static final String PHONE_MAX_LENGTH = "Phone number exceeds maximum length of 11 characters.";
    public static final String PHONE_NUMERIC_ONLY = "Phone number accepts numeric values only.";
    public static final String RESIGN = "RESIGN";
    public static final String ACCOUNT_LOCKED_MESSAGE_WITH_TIME = "The account is locked. You can reset your password to unlock it immediately, or wait %d minute(s) after trying again.";
    public static final String MEMBER_WRONG_BIRTHDATE_2 =
            "Member code and date of birth do not match.\n\n"
                   + "For Individual and Family members, submit to your Account Officer. "
                   + "For Corporate account members, submit to your HR.";
    public static final String MEMBER_WRONG_BIRTHDATE_1 =
            "Member code and date of birth do not match. Please try again.";

   public static final String INVALID_NONMEMBER_DETAILS = "Invalid non-member details";
    public static final String OTP_VERIFIED_SUCCESS = "OTP verified successfully";
    public static final String EMAIL_OR_MOBILE_REQUIRED = "Either email or mobile is required";
    public static final String USER_NOT_FOUND = "User not found with email: ";
    public static final String EMAIL_NOT_REGISTERED_NON_MEMBER = "This email is not registered. Would you like to register instead?";
    public static final String INVALID_MEMBER_DETAILS = "Invalid member details";
    public static final String PASSWORD_SET_SUCCESS_NONMEMBER = "Password set successfully";
    public static final String PASSWORD_RESET_SUCCESS_NONMEMBER = "Password reset successfully for non member ";
    public static final String PASSWORD_SET_SUCCESS_MEMBER = "Password set successfully";
    public static final String PASSWORD_RESET_SUCCESS_MEMBER = "Password reset successfully";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";
    public static final String MEMBER_VALIDATED = "validation successfull";
    public static final String MEMBERSHIP_INACTIVE = "Membership inactive or resigned";
    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";
    public static final String EMAIL_SUBJECT_OTP = "Your OTP Code";
    public static final String LOGIN_EMAIL_SUBJECT_OTP = "Your Login OTP Code";

    public static final String EMAIL_CONTENT_TYPE = "HTML";
    public static final String EMAIL_TYPE_OTP_VERIFICATION = "otp-verification";
    public static final String EMAIL_BODY_TEMPLATE =
            "Dear user,<br/><br/>Your OTP code is: <b>%s</b>. "
                  +  "This OTP is valid for 90 seconds.<br/><br/>"
                  +  "Thank you,<br/>MediCard Team";
    public static final String LOG_PROCESSING_LOGIN = "Processing login for credential: {}";
    public static final String LOG_PROCESSING_LOGIN_MEMBER = "Processing login for member: {}";
    public static final String LOG_INVALID_PASSWORD = "Invalid password attempt for credential: {}";
    public static final String LOG_INVALID_PASSWORD_MEMBER = "Invalid password for member: {}";
    public static final String LOG_INACTIVE_USER = "Inactive user attempted login: {}";
    public static final String LOG_OTP_LOCK_ACTIVE = "Login blocked for member: {} - OTP service lock active";
    public static final String LOG_PASSWORD_LOCK_ACTIVE = "Login attempt for locked member account: {}, remaining lock time: {} minutes";
    public static final String LOG_ACCOUNT_LOCKED = "Account locked for member: {} due to {} failed attempts";
    public static final String LOG_PASSWORD_VALIDATED = "Password validated successfully for member: {}";
    public static final String USER_ALREADY_EXISTS = "User already registered with this member code: ";
    public static final String MEMBER_WRONG_BIRTHDATE_MORE_THEN_THREEATTEMPTS = " Attempt exceeded, please try again after 15 minutes.";

    // Login Attempt Tracking Messages
    public static final String ACCOUNT_LOCKED_PASSWORD_ATTEMPTS = "Account locked due to multiple failed login attempts. Please try again after 15 minutes.";
    public static final String ACCOUNT_LOCKED_PASSWORD_ATTEMPTS_WITH_COUNT = "The account is locked. You can reset your password to unlock it immediately, or wait 15 minute(s) after trying again.";
    public static final String COMMENT_SAVE_TO_RESET_LOCK = "Save to reset if lock expired";

    // Welcome Email Constants
    public static final String EMAIL_SUBJECT_WELCOME = "Welcome to MediCard GO app!";
    public static final String EMAIL_TYPE_WELCOME = "welcome-email";
    public static final String EMAIL_BODY_WELCOME_TEMPLATE =
            "Welcome to MediCard GO app!<br/><br/>"
            + "Thank you for downloading the app and for taking the step toward managing your healthcare with ease. "
            + "We're excited to have you join our growing community of empowered MediCard GO users.<br/><br/>"
            + "With your new account, you now have access to the app's services that are designed to make your healthcare more convenient and accessible:<br/><br/>"
            + "• Quick Letter of Authorization (LOA) Generation<br/>"
            + "• Teleconsultation<br/>"
            + "• Clinic Appointment Booking<br/>"
            + "• Online Reimbursement Submission<br/>"
            + "• Access to Accredited Hospitals and Doctors List<br/><br/>"
            + "All these features and more are accessible right at your fingertips.<br/><br/>"
            + "We'd also love to hear about your experience with our registration process. Your feedback will help us improve and ensure a smooth journey for all users. "
            + "Simply click on the link <a href=\"%s\">[survey link]</a> to answer our quick survey.<br/><br/>"
            + "If you have any questions or need assistance, feel free to contact us at (02) 8841-8080.<br/><br/>"
            + "Thank you for choosing MediCard GO where your healthcare needs are in one ultimate, easy to use app.";
    public static final String SURVEY_LINK_PLACEHOLDER = "https://forms.office.com/r/N8jLQ7aCgN"; // TODO: Replace with actual survey link
}