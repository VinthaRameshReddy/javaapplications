package com.medgo.filemanagement.utils;


public class CustomExceptionHelper {
    public static CustomException uploadingFailed(String message) {
        return new CustomException(String.format("Failed to upload: %s", message));
    }
}