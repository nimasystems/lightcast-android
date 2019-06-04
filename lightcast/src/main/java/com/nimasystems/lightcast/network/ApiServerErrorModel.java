package com.nimasystems.lightcast.network;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.ArrayList;

public class ApiServerErrorModel {
    public int code;
    public String message;
    public String domainName;
    public JSONObject extraData;

    public ArrayList<ApiServerValidationError> validationErrors;

    @NonNull
    @Override
    public String toString() {
        return message;
    }
}
