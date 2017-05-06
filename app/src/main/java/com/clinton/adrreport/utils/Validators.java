package com.clinton.adrreport.utils;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.regex.Pattern;

public class Validators {

    public static boolean isValidEmail(String email){
        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }

    public static boolean isValidPassword(String pass){
        return pass.length() > 4;
    }

    public static boolean isValidPhoneNumber(String phone){
        PhoneNumberUtil phoneNum = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber indianNum = phoneNum.parse(phone, "IN");
            return phoneNum.isValidNumber(indianNum);
        } catch (NumberParseException e) {
            return false;
        }
    }

    public static String formatAsYouType(String phone){
        phone = phone.trim();
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        AsYouTypeFormatter formatter = phoneNumberUtil.getAsYouTypeFormatter("IN");
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < phone.length(); i++) {
            buffer.append(formatter.inputDigit(phone.charAt(i)));
        }
        return buffer.toString();
    }

    public static boolean isNotEmpty(String empty){
        return !empty.trim().equals("");
    }
}
