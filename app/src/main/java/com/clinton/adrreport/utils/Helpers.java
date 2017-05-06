package com.clinton.adrreport.utils;

import android.util.Log;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class Helpers {

    public static final String BASE_URL = "https://adr-server.herokuapp.com/api/";
    public static final String SETTINGS_FILE = "settings_file";

    public static String getMessage(int code) {
        switch (code) {
            case HttpStatus.UNAUTHORIZED:
                return "Username and Password incorrect";
            case HttpStatus.CONFLICT:
                return "An account exists with email id";
            default:
                throw new IllegalArgumentException(String.valueOf(code));
        }
    }

    public static String exceptionMessage(IOException e) {

        if(e instanceof SocketTimeoutException || e instanceof UnknownHostException){
            return "Check internet connection";
        }
        else Log.e("CONNECTION", "didn't catch error", e);
        return null;
    }
}
