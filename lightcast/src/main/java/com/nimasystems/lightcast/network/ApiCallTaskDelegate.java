package com.nimasystems.lightcast.network;

public interface ApiCallTaskDelegate {
    void didFinishTask(ApiCallBase sender);

    void didFinishTaskWithError(ApiCallBase sender, int errorCode,
                                String errorMessage, ApiServerErrorModel serverError);
}