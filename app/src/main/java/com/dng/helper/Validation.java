package com.dng.helper;

import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validation {

    private final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";
    private final String FULLNAME_PATTERN = "^[\\p{L} .'-]+$";
    private final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";

    private Pattern pattern;
    private Matcher matcher;

    private String getString(TextView textView) {
        String getValue = textView.getText().toString().trim();
        return getValue;
    }

    // "use isEmpty instead of the isNullValue"
    public boolean isNullValue(TextView textView) {
        if (getString(textView).isEmpty() && getString(textView).equals("")) {
            return false;
        }
        return true;
    }

    public boolean isEditTextNull(EditText editText) {
        if (editText.getText().toString().trim().equals("")) {
            return false;
        }
        return true;
    }

    public boolean isEmpty(TextView textView) {
        if (getString(textView).isEmpty()) {
           /* textView.setError("feild can't be empty");
            textView.requestFocus();*/
            return true;
        }
        return false;
    }

    public boolean isEmailValid(EditText editText) {
        String getValue = editText.getText().toString().trim();
        return android.util.Patterns.EMAIL_ADDRESS.matcher(getValue).matches();

    }


    public boolean isUserNameValid(TextView textView) {
        pattern = Pattern.compile(USERNAME_PATTERN);
        matcher = pattern.matcher(getString(textView));
        boolean bool = matcher.matches();
        if (!bool) {
            textView.setError("Enter valid userName");
            textView.requestFocus();
        }
        return bool;
    }

    public boolean isFullNameValid(TextView textView) {
        Pattern pattern = Pattern.compile(FULLNAME_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(getString(textView));
        return matcher.find();
    }


    private boolean isPasswordValid(TextView textView) {
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(getString(textView));
        boolean bool = matcher.matches();
        if (!bool) {
            textView.setError("At least 4 characters required");
            textView.requestFocus();
        }
        return bool;
    }

    public boolean isPasswordValid(EditText editText) {
        String getValue = editText.getText().toString().trim();
        return getValue.length() >= 6;
    }

    public boolean isEmailValid(TextView textView) {
        boolean bool = android.util.Patterns.EMAIL_ADDRESS.matcher(getString(textView)).matches();
        if (!bool) {
            textView.setError("Enter valid email");
            textView.requestFocus();
        }
        return bool;
    }

}
