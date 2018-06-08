package com.nimasystems.lightcast.network;

import org.json.JSONObject;

interface UnauthorizedInterceptorListener {
    void OnUnauthorizedInterceptorAction(int responseCode, JSONObject responseJSON);
}
