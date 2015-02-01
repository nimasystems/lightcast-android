package com.nimasystems.lightcast.network;

import java.util.ArrayList;

public class ApiServerErrorModel {
    public int code;
    public String message;
    public String domainName;

    public ArrayList<ApiServerValidationError> validationErrors;
}
