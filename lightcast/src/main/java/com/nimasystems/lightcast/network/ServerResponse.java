/*
 * While Clicking Favorite button in Nutrition Sub Catorgory
 * Its used to Update the favorite task form backend
 * After getting success response status will updated for the corressponding item
 * */
package com.nimasystems.lightcast.network;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ServerResponse<T> {
    private final boolean isSuccessful;
    private HashMap<String, Object> data;
    private ApiServerErrorModel serverError;

    private int fetchedDataCount;
    private List<T> fetchedData;

    public ServerResponse(final T fetchedDataItem) {
        this.fetchedData = new ArrayList<T>() {{
            add(fetchedDataItem);
        }};
        this.fetchedDataCount = 1;
        isSuccessful = true;
    }

    public ServerResponse(final List<T> fetchedData, int fetchedDataCount) {
        this.fetchedData = fetchedData;
        this.fetchedDataCount = fetchedDataCount;
        isSuccessful = true;
    }

    public ServerResponse(final ApiServerErrorModel serverError) {
        this.serverError = serverError;
        isSuccessful = false;
    }

    public int getCode() {
        return serverError != null ? serverError.code : 0;
    }

    public String getMessage() {
        return serverError != null ? serverError.message : null;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public ApiServerErrorModel getServerError() {
        return serverError;
    }

    public int getInt(@NonNull String key) {
        Integer d = data != null ? (Integer) data.get(key) : null;
        return d != null ? d : 0;
    }

    public float getFloat(@NonNull String key) {
        Float d = data != null ? (Float) data.get(key) : null;
        return d != null ? d : 0;
    }

    public double getDouble(@NonNull String key) {
        Double d = data != null ? (Double) data.get(key) : null;
        return d != null ? d : 0;
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
        Boolean d = data != null ? (Boolean) data.get(key) : null;
        return d != null ? d : false;
    }

    public HashMap<String, Object> getMap(@NonNull String key) {
        //noinspection unchecked
        return data != null ? (HashMap<String, Object>) data.get(key) : null;
    }

    public List<HashMap<String, Object>> getMapList(@NonNull String key) {
        //noinspection unchecked
        return data != null ? (List<HashMap<String, Object>>) data.get(key) : null;
    }

    public int getFetchedDataCount() {
        return fetchedDataCount;
    }

    public List<T> getFetchedData() {
        return fetchedData;
    }

    public T getFirstObject() {
        return fetchedData != null && fetchedData.size() > 0 ? fetchedData.get(0) : null;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }
}