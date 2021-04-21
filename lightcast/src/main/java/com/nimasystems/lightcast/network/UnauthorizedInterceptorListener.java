package com.nimasystems.lightcast.network;

import org.json.JSONObject;

public interface UnauthorizedInterceptorListener {
    void OnUnauthorizedInterceptorAction(int responseCode, JSONObject responseJSON);
}
