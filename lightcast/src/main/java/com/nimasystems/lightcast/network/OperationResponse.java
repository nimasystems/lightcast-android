/*
 * While Clicking Favorite button in Nutrition Sub Catorgory
 * Its used to Update the favorite task form backend
 * After getting success response status will updated for the corressponding item
 * */
package com.nimasystems.lightcast.network;

public class OperationResponse {
    private int code;
    private String message;
    private Object data;
    private ApiServerErrorModel serverError;

    public OperationResponse(Object data) {
        this.data = data;
    }

    public OperationResponse(int code, String message, ApiServerErrorModel serverError) {
        this.code = code;
        this.message = message;
        this.serverError = serverError;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public ApiServerErrorModel getServerError() {
        return serverError;
    }
}