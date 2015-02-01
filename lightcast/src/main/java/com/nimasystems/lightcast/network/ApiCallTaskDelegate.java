package com.nimasystems.lightcast.network;

public interface ApiCallTaskDelegate {
    public void didFinishTask(ApiCallBase sender);

    public void didFinishTaskWithError(ApiCallBase sender, int errorCode,
                                       String errorMessage, ApiServerErrorModel serverError);
}