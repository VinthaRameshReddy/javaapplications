package com.medgo.facescan.constant;

public class FaceScanErrorMessages {
    public FaceScanErrorMessages() {
    }

    //eligibility
    public static final String MEMBER_CODE_NULL_OR_EMPTY = "Member code cannot be null or empty";
    public static final String MEMBER_CODE_NOT_FOUND = "Member code not found";
    public static final String NOT_CORPORATE_USER = "Customer is not a corporate user";
    public static final String FACE_SCAN_LIMIT_EXCEEDED = "Face scan limit exceeded. Try again after %s";
    public static final String DATABASE_ERROR = "Database error";
    public static final String UNEXPECTED_ERROR = "Unexpected error occurred";

    //accept tnc
    public static final String FED_ID_NULL_OR_EMPTY = "Fed ID cannot be null or empty";
    public static final String TNC_NOT_ACCEPTED = "Terms and Conditions not accepted";
    public static final String SESSION_NOT_FOUND_FOR_FED_ID = "Session not found for the given fedId";


    public static final String SESSION_ID_NULL_OR_EMPTY = "Session ID cannot be null or empty";
    public static final String FACE_SCAN_SESSION_NOT_FOUND = "Face scan session not found";
    public static final String INVALID_END_TIME_FORMAT = "Invalid end time format";
    public static final String FAILED_TO_SERIALIZE_SCAN_DATA = "Failed to serialize scan data";

    public static final String NO_FACE_SCAN_HISTORY_FOUND = "No face scan history found";
    public static final String FACE_SCAN_HISTORY_RETRIEVE_SUCCESS = "Face scan history retrieved successfully.";



}
