package com.nimasystems.lightcast.network;

import org.json.JSONObject;

import java.util.ArrayList;

public class ApiServerErrorModel {
    public int code;
    public String message;
    public String domainName;
    public JSONObject extraData;

    public ArrayList<ApiServerValidationError> validationErrors;

    @Override
    public String toString() {
        return message;
    }
}
