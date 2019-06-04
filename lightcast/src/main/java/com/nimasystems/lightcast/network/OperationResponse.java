/*
 * While Clicking Favorite button in Nutrition Sub Catorgory
 * Its used to Update the favorite task form backend
 * After getting success response status will updated for the corressponding item
 * */
package com.nimasystems.lightcast.network;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class OperationResponse {
    private int code;
    private String message;
    private HashMap<String, Object> data;
    private ApiServerErrorModel serverError;

    public OperationResponse(HashMap<String, Object> data) {
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

    public HashMap<String, Object> getData() {
        return data;
    }

    public ApiServerErrorModel getServerError() {
        return serverError;
    }

    public int getInt(@NonNull String key) {
        return data != null ? (int) data.get(key) : 0;
    }

    public float getFloat(@NonNull String key) {
        return data != null ? (float) data.get(key) : 0.0f;
    }

    public double getDouble(@NonNull String key) {
        return data != null ? (double) data.get(key) : 0.0f;
    }

    public List<?> getList(@NonNull String key) {
        return data != null ? (List<?>) data.get(key) : null;
    }

    public Date getDate(@NonNull String key) {
        return data != null ? (Date) data.get(key) : null;
    }

    public String getString(@NonNull String key) {
        return data != null ? (String) data.get(key) : "";
    }

    public boolean getBoolean(@NonNull String key) {
        return data != null && (boolean) data.get(key);
    }

    public HashMap<String, Object> getMap(@NonNull String key) {
        //noinspection unchecked
        return data != null ? (HashMap<String, Object>) data.get(key) : null;
    }

    public List<HashMap<String, Object>> getMapList(@NonNull String key) {
        //noinspection unchecked
        return data != null ? (List<HashMap<String, Object>>) data.get(key) : null;
    }
}