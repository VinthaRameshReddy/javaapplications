package com.medgo.facescan.constant;


import com.medgo.facescan.types.PortalUserStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class CoreConstants {

    public static final String AUTH_INVALID_STATUS_FORMAT = "The account is %s. Please wait 15 minutes before you try again.";
    public static final String AUTH_LOCKED_STATUS_FORMAT = String.format(
            AUTH_INVALID_STATUS_FORMAT,
            PortalUserStatusEnum.LOCKED.toString().toUpperCase()
    );

    public static final String RESIGNED_EFF = "RESIGNED - EFF. ";
}