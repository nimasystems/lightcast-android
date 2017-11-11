package com.nimasystems.lightcast.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.common.RequestBuilder;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndJSONObjectRequestListener;
import com.nimasystems.lightcast.encryption.AESCrypt;
import com.nimasystems.lightcast.utils.DebugUtils;
import com.nimasystems.lightcast.utils.StringUtils;

import org.cryptonode.jncryptor.AES256JNCryptor;
import org.cryptonode.jncryptor.JNCryptor;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Route;

abstract public class ApiCallBase {

    public enum RequestType {

        Get(1), Post(2), Put(3), Delete(4);

        private final int value;

        RequestType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static final String DEFAULT_REQUEST_CHARSET = "UTF-8";
    public static final int DEFAULT_MAX_RETRIES = 3;

    public static final String DEFAULT_XDG_HEADER_NAME = "X-Device-UUID";
    public static final String DEFAULT_XAT_HEADER_NAME = "X-User-Ac";
    public static final String DEFAULT_X_CLIENT_API_LEVEL_HEADER_NAME = "X-App-Version";
    public static final String DEFAULT_XTZ_HEADER_NAME = "X-TZ";

    public static final String DEFAULT_ACCEPTED_MIMETYPE = "application/json";

    public static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    public static final int DEFAULT_READ_TIMEOUT = 30000;
    public static final int DEFAULT_WRITE_TIMEOUT = 10000;
    public static final int DEFAULT_MAX_RETRIES_TIMEOUT = 20000;

    protected int mConnectTimeout = DEFAULT_CONNECT_TIMEOUT;
    protected int mReadTimeout = DEFAULT_READ_TIMEOUT;
    protected int mWriteTimeout = DEFAULT_READ_TIMEOUT;

    protected final Logger mLogger = LoggerFactory.getLogger(this.getClass());

    protected List<Header> mRequestHeaders;
    protected String mConnectionUrl;
    protected String mRequestUserAgent;
    protected int mResponseStatusCode;
    protected List<Header> mResponseHeaders;
    protected ResponseBody mResponseBody;
    protected JSONObject mResponseJson;
    protected boolean mResponseIsSuccess;
    protected Context mContext;
    protected String mAcceptedMimetype = DEFAULT_ACCEPTED_MIMETYPE;
    protected int mClientAPILevel;
    protected String mServerHostname;
    protected String mServerAddress;
    protected String mQueryPathPrefix;
    protected String mAccessToken;
    protected String mDeviceFriendlyName;
    protected String mDeviceGuid;
    protected String mOldDeviceGuid;
    protected String mLocale;

    private String oAuth2BearerToken;
    private boolean oAuth2AuthenticationEnabled;

    protected boolean mCallbackOnMainThread = true;
    protected boolean mSynchronizedCallback;
    protected ApiServerErrorModel mServerError;
    protected boolean mIsSynchronous;
    protected boolean mIsCancelled;
    protected boolean mIsSuccessful;
    protected int mLastErrorCode;
    protected String mLastErrorMessage;
    protected ApiCallTaskDelegate mDelegate;
    protected boolean mUseSSL;
    protected boolean mSSLTrustAll;

    private Executor mExecutor;
    private boolean mDebug;
    private String mConnectionId;
    private boolean mHttpAuthEnabled;
    private String mHttpAuthUsername;
    private String mHttpAuthPassword;
    private boolean mIsBusy;
    private TimeZone mServerTimezone;

    private String mDeviceGuidHeaderName = DEFAULT_XDG_HEADER_NAME;

    private String mUserAccessTokenHeaderName = DEFAULT_XAT_HEADER_NAME;

    private String mTimezoneHeaderName = DEFAULT_XTZ_HEADER_NAME;

    private String mAppVersionHeaderName = DEFAULT_X_CLIENT_API_LEVEL_HEADER_NAME;

    public String getObjectTag() {
        return objectTag;
    }

    private String objectTag;

    private ApiCallBaseErrorFilter errorFilterListener;

    public interface ApiCallBaseErrorFilter {
        boolean onError(@NonNull final ApiCallBase apiCall, final ApiServerErrorModel error);
    }

    public ApiCallBase(Context context) {
        mContext = context;
        mRequestHeaders = new ArrayList<>();
        objectTag = UUID.randomUUID().toString();
    }

    public ApiCallBase(Context context, ApiCallTaskDelegate delegate) {
        mContext = context;
        mDelegate = delegate;
        mRequestHeaders = new ArrayList<>();
    }

    public ApiCallBase setDelegate(ApiCallTaskDelegate delegate) {
        mDelegate = delegate;
        return this;
    }

    public static String encrDid(String deviceGuid, String deviceFriendlyName, String oldDeviceGuid) {

        if (StringUtils.isNullOrEmpty(deviceGuid)) {
            return null;
        }

        String friendlyName = !StringUtils.isNullOrEmpty(deviceFriendlyName) ? deviceFriendlyName
                : "-unknown-";
        String joined = deviceGuid + "|" + friendlyName + "|" + "android" +
                (!StringUtils.isNullOrEmpty(oldDeviceGuid) ? "|" + oldDeviceGuid : "");

        String tkr;

        try {
            tkr = new AESCrypt().encrypt(joined);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return tkr;
    }

    public static String encrDid2(String deviceGuid, String deviceFriendlyName, String oldDeviceGuid, String encrKey) {

        if (StringUtils.isNullOrEmpty(deviceGuid) || StringUtils.isNullOrEmpty(encrKey)) {
            return null;
        }

        String friendlyName = !StringUtils.isNullOrEmpty(deviceFriendlyName) ? deviceFriendlyName
                : "-unknown-";
        String joined = deviceGuid + "|" + friendlyName + "|" + "android" +
                (!StringUtils.isNullOrEmpty(oldDeviceGuid) ? "|" + oldDeviceGuid : "");

        String tkr = null;

        JNCryptor cryptor = new AES256JNCryptor();
        byte[] plaintext = joined.getBytes();

        try {
            byte[] ct = cryptor.encryptData(plaintext, encrKey.toCharArray());
            tkr = new String(android.util.Base64.encode(ct, android.util.Base64.DEFAULT), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tkr;
    }

    public ApiCallBase setErrorFilterListener(ApiCallBaseErrorFilter errorFilterListener) {
        this.errorFilterListener = errorFilterListener;
        return this;
    }

    protected static ApiServerErrorModel parseResponseForError(JSONObject obj) {

        ApiServerErrorModel err;

        if (obj != null) {
            if (obj.has("error")) {
                JSONObject objError = obj.optJSONObject("error");

                if (objError != null) {
                    String domain = objError.optString("domain");
                    Integer code = objError.optInt("code");
                    String message = objError.optString("message");

                    err = new ApiServerErrorModel();
                    err.code = code;
                    err.domainName = domain;
                    err.message = message;
                    err.extraData = objError.optJSONObject("data");

                    // validation errors
                    JSONObject vJErrs = objError
                            .optJSONObject("validation_errors");
                    if (vJErrs != null) {
                        ArrayList<ApiServerValidationError> vErrs = new ArrayList<>();
                        ApiServerValidationError verr;

                        Iterator<?> keys = vJErrs.keys();
                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            String error = vJErrs.optString(key);

                            verr = new ApiServerValidationError();
                            verr.fieldName = key;
                            verr.message = error;
                            vErrs.add(verr);
                        }

                        err.validationErrors = vErrs;
                    }

                    return err;
                }
            }
        }

        return null;
    }

    protected boolean parseJSONResponse(@NonNull JSONObject response) {
        return true;
    }

    abstract protected String getRequestQueryPath();

    public String getConnectionId() {
        return mConnectionId;
    }

    public ApiCallBase setSynchronizedCallback(boolean syncCallback) {
        mSynchronizedCallback = syncCallback;
        return this;
    }

    public void setoAuth2BearerToken(String oAuth2BearerToken) {
        this.oAuth2BearerToken = oAuth2BearerToken;
    }

    public void setoAuth2AuthenticationEnabled(boolean oAuth2AuthenticationEnabled) {
        this.oAuth2AuthenticationEnabled = oAuth2AuthenticationEnabled;
    }

    public ApiCallBase setExecutor(Executor mExecutor) {
        this.mExecutor = mExecutor;
        return this;
    }

    public ApiCallBase setCallbackOnMainThread(boolean callbackOnMainThread) {
        this.mCallbackOnMainThread = callbackOnMainThread;
        return this;
    }

    public String getRequestPreparedQueryPath() {
        String ret = null;
        String qp = getRequestQueryPath();
        String query = getRequestQuery();

        if (qp != null) {
            ret = (!StringUtils.isNullOrEmpty(this.mQueryPathPrefix) ? this.mQueryPathPrefix : "") +
                    qp +
                    (query != null ? "?" + query : "");
        }

        return ret;
    }

    protected String getRequestQuery() {
        return null;
    }

    public int getClientAPILevel() {
        return mClientAPILevel;
    }

    public ApiCallBase setClientAPILevel(int apiLevel) {
        mClientAPILevel = apiLevel;
        return this;
    }

    public String getHttpAuthUsername() {
        return mHttpAuthUsername;
    }

    public ApiCallBase setHttpAuthUsername(String username) {
        mHttpAuthUsername = username;
        return this;
    }

    public String getHttpAuthPassword() {
        return mHttpAuthPassword;
    }

    public ApiCallBase setHttpAuthPassword(String password) {
        mHttpAuthPassword = password;
        return this;
    }

    public boolean getHttpAuthEnabled() {
        return mHttpAuthEnabled;
    }

    public ApiCallBase setHttpAuthEnabled(boolean enabled) {
        mHttpAuthEnabled = enabled;
        return this;
    }

    public boolean getSSLTrustAll() {
        return mSSLTrustAll;
    }

    public ApiCallBase setSSLTrustAll(boolean trustAll) {
        mSSLTrustAll = trustAll;
        return this;
    }

    public String getRequestUserAgent() {
        return mRequestUserAgent;
    }

    public ApiCallBase setRequestUserAgent(String ua) {
        mRequestUserAgent = ua;
        return this;
    }

    public boolean getDebug() {
        return mDebug;
    }

    public ApiCallBase setDebug(boolean debug) {
        mDebug = debug;
        return this;
    }

    protected void log(String str) {
        if (mLogger != null) {
            mLogger.info(mConnectionId + ": " + str);
        }
    }

    protected void logError(String str) {
        if (mLogger != null) {
            mLogger.error(mConnectionId + ": " + str);
        }
    }

    protected void logDebug(String str) {
        if (mLogger != null) {
            mLogger.debug(mConnectionId + ": " + str);
        }
    }

    protected boolean initialize() {
        return true;
    }

    public ApiCallBase setConnectTimeout(int timeout) {
        mConnectTimeout = timeout;
        return this;
    }

    public ApiCallBase setReadTimeout(int timeout) {
        mReadTimeout = timeout;
        return this;
    }

    public TimeZone getServerTimezone() {
        return mServerTimezone;
    }

    public ApiServerErrorModel getServerError() {
        return mServerError;
    }

    public JSONObject getResponseJson() {
        return mResponseJson;
    }

    public ResponseBody getResponseBody() {
        return mResponseBody;
    }

    public int getResponseStatusCode() {
        return mResponseStatusCode;
    }

    public List<Header> getResponseHeaders() {
        return mResponseHeaders;
    }

    public boolean getResponseIsSuccess() {
        return mResponseIsSuccess;
    }

    public boolean getIsBusy() {
        return mIsBusy;
    }

    public ApiCallBase setUseSSL(boolean useSSL) {
        mUseSSL = useSSL;
        return this;
    }

    public void setDeviceGuidHeaderName(String deviceGuidHeaderName) {
        this.mDeviceGuidHeaderName = deviceGuidHeaderName;
    }

    public void setUserAccessTokenHeaderName(String userAccessTokenHeaderName) {
        this.mUserAccessTokenHeaderName = userAccessTokenHeaderName;
    }

    public void setTimezoneHeaderName(String timezoneHeaderName) {
        this.mTimezoneHeaderName = timezoneHeaderName;
    }

    public void setAppVersionHeaderName(String appVersionHeaderName) {
        this.mAppVersionHeaderName = appVersionHeaderName;
    }

    protected RequestType getRequestType() {
        return RequestType.Get;
    }

    public ApiCallBase setAccessToken(String accessToken) {
        mAccessToken = accessToken;
        return this;
    }

    public ApiCallBase setServerHostname(String hostname) {
        mServerHostname = hostname;
        return this;
    }

    public ApiCallBase setServerAddress(String address) {
        mServerAddress = address;
        return this;
    }

    public ApiCallBase setQueryPathPrefix(String prefix) {
        mQueryPathPrefix = prefix;
        return this;
    }

    public ApiCallBase setDeviceFriendlyName(String friendlyName) {
        mDeviceFriendlyName = friendlyName;
        return this;
    }

    public ApiCallBase setDeviceGuid(String deviceGuid) {
        mDeviceGuid = deviceGuid;
        return this;
    }

    public ApiCallBase setOldDeviceGuid(String deviceGuid) {
        mOldDeviceGuid = deviceGuid;
        return this;
    }

    public ApiCallBase setLocale(String locale) {
        mLocale = locale;
        return this;
    }

    public boolean isSuccessful() {
        return mIsSuccessful;
    }

    protected boolean validateRequest() {
        return true;
    }

    protected boolean getIncludeVerification() {
        return true;
    }

    public String getLastErrorMessage() {
        return mLastErrorMessage;
    }

    public int getLastErrorCode() {
        return mLastErrorCode;
    }

    public synchronized String getDeviceIdEncrypted(boolean useSecondMethod, String key) {
        // encrypt / hash the device id
        String deviceId;

        if (useSecondMethod) {
            deviceId = encrDid2(mDeviceGuid, mDeviceFriendlyName, mOldDeviceGuid, key);
        } else {
            deviceId = encrDid(mDeviceGuid, mDeviceFriendlyName, mOldDeviceGuid);
        }

        return escapeHeaderValue(deviceId);
    }

    protected void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        System.setProperty("http.keepAlive", "false");
    }

    public List<Header> getRequestHeaders() {
        return mRequestHeaders;
    }

    protected Header[] getAllHeaders() {
        List<Header> headers = prepareRequestHeaders();
        return headers.toArray(new Header[headers.size()]);
    }

    protected boolean requiresUserAuthorization() {
        return false;
    }

    protected List<Header> prepareRequestHeaders() {
        return mRequestHeaders;
    }

    protected String getRequestCharset() {
        return DEFAULT_REQUEST_CHARSET;
    }

    protected void prepareMyHeaders(String serverHostname, String charset) {

        String hostnameEnc = escapeHeaderValue(serverHostname);
        String accessToken = mAccessToken;

        // charset
        mRequestHeaders.add(new Header("Charset", charset
                .toLowerCase(Locale.US)));

        // host
        if (!StringUtils.isNullOrEmpty(hostnameEnc)) {
            mRequestHeaders.add(new Header("Host", hostnameEnc));
        }

        // user agent
        if (!StringUtils.isNullOrEmpty(mRequestUserAgent)) {
            mRequestHeaders
                    .add(new Header("User-Agent", mRequestUserAgent));
        }

        // access token
        if (!StringUtils.isNullOrEmpty(accessToken)) {
            mRequestHeaders.add(new Header(mUserAccessTokenHeaderName, accessToken));
        }

        // OAuth2
        if (oAuth2AuthenticationEnabled && !StringUtils.isNullOrEmpty(oAuth2BearerToken)) {
            mRequestHeaders.add(new Header("Authorization", "Bearer " + oAuth2BearerToken));
        }

        // device id
        String dIdEnc = mDeviceGuid;

        if (!StringUtils.isNullOrEmpty(dIdEnc)) {
            mRequestHeaders.add(new Header(mDeviceGuidHeaderName, dIdEnc));
        }

        // client API Level
        if (mClientAPILevel > 0) {
            mRequestHeaders.add(new Header(mAppVersionHeaderName, String.valueOf(mClientAPILevel)));
        }

        // accept language fixing
        String ac = mLocale;
        ac = (ac != null) ? ac.replace('_', '-') : null;

        if (!StringUtils.isNullOrEmpty(ac)) {
            //noinspection ConstantConditions
            mRequestHeaders.add(new Header("Accept-Language", ac != null ? ac
                    .toLowerCase(Locale.US) : null));
        }

        // let the server know we expect JSON
        if (!StringUtils.isNullOrEmpty(mAcceptedMimetype)) {
            mRequestHeaders.add(new Header("Accept", mAcceptedMimetype));
        }
    }

    protected void onConnectionStart() {
        logDebug("Connection started (" + mConnectionUrl + ")");
    }

    protected void onConnectionFinish() {
        logDebug("Connection finish (" + mConnectionUrl + ")");
    }

    protected void onConnectionCancel() {
        logDebug("Connection cancel (" + mConnectionUrl + ")");
    }

    protected void onConnectionSuccess(Response okHttpResponse, JSONObject response) {
        mResponseStatusCode = okHttpResponse.code();
        mResponseHeaders = makeResponseHeaders(okHttpResponse.headers());
        mResponseBody = okHttpResponse.body();
        mResponseJson = response;
        mResponseIsSuccess = true;

        logDebug("Connection success (" + mConnectionUrl + "), Status code: "
                + mResponseStatusCode);

        doPostOperations(true);
    }

    protected void onConnectionFailure(ANError anError) {
        Response response = anError != null ? anError.getResponse() : null;

        String errorBody = anError != null ? anError.getErrorBody() : null;

        mResponseJson = null;

        if (errorBody != null) {
            try {
                mResponseJson = new JSONObject(errorBody);
            } catch (JSONException e) {
                //
            }
        }

        mResponseStatusCode = response != null ? response.code() : 0;
        mResponseHeaders = makeResponseHeaders(response != null ? response.headers() : null);
        mResponseBody = response != null ? response.body() : null;
        mResponseIsSuccess = false;

        logDebug("Connection failure (" + mConnectionUrl + "), Status code: "
                + mResponseStatusCode + ", Error: " +
                (anError != null ? anError.getErrorBody() : ""));

        doPostOperations(false);
    }

    protected List<Header> makeResponseHeaders(Headers headers) {
        if (headers == null || headers.size() < 1) {
            return null;
        }

        int i;

        List<Header> allh = new ArrayList<>();

        for (i = 0; i <= headers.size() - 1; i++) {
            allh.add(new Header(headers.name(i), headers.value(i)));
        }

        return allh;
    }

    public Header getResponseHeader(String name) {
        if (mResponseHeaders == null) {
            return null;
        }

        Header h = null;
        String n;

        for (Header ah : mResponseHeaders) {
            n = ah.getKey();
            if (n.equalsIgnoreCase(name)) {
                h = ah;
                break;
            }
        }

        return h;
    }

    public String getResponseHeaderValue(String name) {
        Header header = getResponseHeader(name);
        return (header != null ? header.getValue() : null);
    }

    protected void parseResponseHeaders() {

        if (mResponseHeaders == null) {
            return;
        }

        // timezone
        String tzCode = getResponseHeaderValue(mTimezoneHeaderName);

        if (!StringUtils.isNullOrEmpty(tzCode)) {
            mServerTimezone = TimeZone.getTimeZone(tzCode);
        }
    }

    protected void doPostOperations(boolean isConnectionSuccess) {
        parseResponseHeaders();

        boolean parseSuccess = parseResponse();

        mIsBusy = false;
        mIsSuccessful = isConnectionSuccess && parseSuccess;

        // notify the delegate
        if (mDelegate != null) {
            if (mCallbackOnMainThread) {
                if (!mSynchronizedCallback && (mIsSynchronous || isOnMainThread())) {
                    if (isOnMainThread()) {
                        if (mIsSuccessful) {
                            mDelegate.didFinishTask(this);
                        } else {
                            boolean shouldProcess = errorFilterListener == null || errorFilterListener.onError(this, mServerError);

                            if (shouldProcess) {
                                mDelegate.didFinishTaskWithError(this, mLastErrorCode,
                                        mLastErrorMessage, mServerError);
                            }
                        }
                    } else {
                        Handler mainHandler = new Handler(mContext.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            public void run() {
                                if (mIsSuccessful) {
                                    mDelegate.didFinishTask(ApiCallBase.this);
                                } else {
                                    boolean shouldProcess = errorFilterListener == null || errorFilterListener.onError(ApiCallBase.this, mServerError);

                                    if (shouldProcess) {
                                        mDelegate.didFinishTaskWithError(ApiCallBase.this, mLastErrorCode,
                                                mLastErrorMessage, mServerError);
                                    }
                                }
                            }
                        };
                        mainHandler.post(myRunnable);
                    }
                } else {
                    Handler mainHandler = new Handler(mContext.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        public void run() {
                            if (mIsSuccessful) {
                                mDelegate.didFinishTask(ApiCallBase.this);
                            } else {
                                boolean shouldProcess = errorFilterListener == null || errorFilterListener.onError(ApiCallBase.this, mServerError);

                                if (shouldProcess) {
                                    mDelegate.didFinishTaskWithError(ApiCallBase.this,
                                            mLastErrorCode, mLastErrorMessage,
                                            mServerError);
                                }
                            }
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            } else {
                if (mIsSuccessful) {
                    mDelegate.didFinishTask(ApiCallBase.this);
                } else {
                    boolean shouldProcess = errorFilterListener == null || errorFilterListener.onError(this, mServerError);

                    if (shouldProcess) {
                        mDelegate.didFinishTaskWithError(ApiCallBase.this,
                                mLastErrorCode, mLastErrorMessage,
                                mServerError);
                    }
                }
            }
        }
    }

    protected boolean isSynchronous() {
        return mIsSynchronous;
    }

    protected boolean needsSynchronization() {
        return !isOnMainThread();
    }

    protected boolean isOnMainThread() {
        return (Looper.myLooper() == Looper.getMainLooper());
    }

    protected boolean parseResponse() {

        boolean isSuccessful = false;

        try {
            if (mResponseJson != null) {
                logDebug("HTTP Response: " + mResponseJson.toString());

                // parse for a server error
                mServerError = parseResponseForError(mResponseJson);

                if (mServerError != null) {
                    isSuccessful = false;
                    mLastErrorCode = mServerError.code;
                    mLastErrorMessage = mServerError.message;
                } else {
                    isSuccessful = parseJSONResponse(mResponseJson);
                }
            } else {
                isSuccessful = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            logError("Unhandled exception while parsing the response: " + e);

            mLastErrorMessage = e.getMessage();
        }

        return isSuccessful;
    }

    protected String getRequestContentType() {
        return null;
    }

    protected RequestParams getRequestParams() {
        return null;
    }

    protected int getRequestMaxRetries() {
        return DEFAULT_MAX_RETRIES;
    }

    protected int getRequestMaxRetriesTimeout() {
        return DEFAULT_MAX_RETRIES_TIMEOUT;
    }

    protected String getConnectionUrl(boolean useSSL, String serverAddress,
                                      String serverHostname, String queryPath, String requestQuery) {
        return (useSSL ? "https://" : "http://")
                + (!StringUtils.isNullOrEmpty(serverAddress) ? serverAddress
                : serverHostname)
                + (!StringUtils.isNullOrEmpty(queryPath) ? queryPath : "")
                + (!StringUtils.isNullOrEmpty(requestQuery) ? "?"
                + requestQuery : "");
    }

    protected void beforeExecute() {
        //
    }

    protected boolean verifyCallParams() {
        return true;
    }

    protected String filterServerHostname(String serverHostname) {
        return serverHostname;
    }

    protected String filterServerAddress(String serverAddress) {
        return serverAddress;
    }

    protected boolean filterUseSSL(boolean useSSL) {
        return useSSL;
    }

    protected OkHttpClient.Builder getHttpClientBuilder() {
        return new OkHttpClient().newBuilder();
    }

    protected HashMap<String, String> preparedParams(RequestParams rp) {
        HashMap<String, String> ret = new HashMap<>();

        RequestParams requestParams = rp != null ? filterRequestParams(rp) : null;

        if (requestParams != null) {
            ConcurrentHashMap<String, String> p1 = requestParams.getUrlParams();

            for (String key : p1.keySet()) {
                String val = p1.get(key);

                if (val != null && !StringUtils.isNullOrEmpty(val) && !val.equals("0")) {
                    ret.put(key, val);
                }
            }

            ConcurrentHashMap<String, Object> p2 = requestParams.getUrlParamsWithObjects();

            for (String key : p2.keySet()) {
                Object val = p2.get(key);
                String v = "";

                if (val instanceof Boolean) {
                    v = ((Boolean) val) ? "1" : "";
                } else if (val instanceof Number) {
                    v = String.valueOf(val);
                    v = v != null && v.equals("0") ? "" : v;
                }

                if (!StringUtils.isNullOrEmpty(v)) {
                    ret.put(key, v);
                }
            }
        }

        return ret;
    }

    @SuppressLint("ObsoleteSdkInt")
    protected boolean executeInternal(boolean synchronous,
                                      String serverHostname, String serverAddress, boolean useSSL) {

        if (!verifyCallParams()) {
            if (mDebug) {
                DebugUtils.ass(false, "call params verification failed");
            }
            return false;
        }

        // check authorization
        if (requiresUserAuthorization() && StringUtils.isNullOrEmpty(mDeviceGuid)) {
            return false;
        }

        beforeExecute();

        serverHostname = filterServerHostname(serverHostname);
        serverAddress = filterServerAddress(serverAddress);
        useSSL = filterUseSSL(useSSL);

        if (StringUtils.isNullOrEmpty(serverHostname)) {
            if (mDebug) {
                DebugUtils.ass(false, "invalid hostname");
            }
            return false;
        }

        if (mIsBusy) {
            // already running
            return false;
        }

        if (!initialize()) {
            if (mDebug) {
                DebugUtils.ass(false, "initialize failed");
            }
            return false;
        }

        if (!validateRequest()) {
            if (mDebug) {
                DebugUtils.ass(false, "validate request failed");
            }
            return false;
        }

        mIsBusy = true;
        mIsSynchronous = synchronous;

        mConnectionId = StringUtils.getRandomString();
        mIsCancelled = false;
        mLastErrorCode = 0;
        mLastErrorMessage = "";
        mIsSuccessful = false;
        mServerError = null;

        RequestType rt = getRequestType();
        rt = rt == null ? RequestType.Get : rt;

        String queryPath = getRequestPreparedQueryPath();

        // prepare the url
        mConnectionUrl = getConnectionUrl(useSSL, serverAddress,
                serverHostname, queryPath, null);

        logDebug("URL: " + mConnectionUrl);

        String charset = getRequestCharset();

        // http://stackoverflow.com/questions/2172752/httpsurlconnection-and-intermittent-connections
        if (useSSL || Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            disableConnectionReuseIfNecessary();
        }

        boolean trustSSL = (!mUseSSL || mSSLTrustAll);

        // prepare the request
        RequestBuilder builder;

        RequestParams rparams = getRequestParams();
        HashMap<String, String> preparedRequestParams = preparedParams(rparams);

        ConcurrentHashMap<String, RequestParams.FileWrapper> fparams = rparams != null ? rparams.getFileParams() : null;
        boolean requestIsMultipart = fparams != null && fparams.size() > 0;

        if (requestIsMultipart) {
            builder = new ANRequest.MultiPartBuilder<>(mConnectionUrl);

            //noinspection ConstantConditions
            if (builder instanceof ANRequest.MultiPartBuilder) {
                ANRequest.MultiPartBuilder bb = (ANRequest.MultiPartBuilder) builder;
                bb.addMultipartParameter(preparedRequestParams);

                for (String key : fparams.keySet()) {
                    bb.addMultipartFile(key, fparams.get(key).file);
                }
            }

        } else if (rt == RequestType.Post) {
            builder = new ANRequest.PostRequestBuilder<>(mConnectionUrl);

            if (preparedRequestParams != null) {
                ((ANRequest.PostRequestBuilder) builder).addBodyParameter(preparedRequestParams);
            }

        } else if (rt == RequestType.Put) {
            builder = new ANRequest.PutRequestBuilder(mConnectionUrl);

            if (preparedRequestParams != null) {
                ((ANRequest.PostRequestBuilder) builder).addBodyParameter(preparedRequestParams);
            }
        } else if (rt == RequestType.Delete) {
            builder = new ANRequest.DeleteRequestBuilder(mConnectionUrl);

            if (preparedRequestParams != null) {
                ((ANRequest.PostRequestBuilder) builder).addBodyParameter(preparedRequestParams);
            }
        } else {
            builder = new ANRequest.GetRequestBuilder(mConnectionUrl);

            if (preparedRequestParams != null) {
                builder.addQueryParameter(preparedRequestParams);
            }
        }

        // set the headers
        prepareMyHeaders(serverHostname, charset);

        Header[] allHeaders = getAllHeaders();

        if (allHeaders != null) {
            logDebug("Prepared headers");

            for (Header h : allHeaders) {
                logDebug(h.getKey() + ": " + h.getValue());
            }
        }

        if (allHeaders != null) {
            for (Header header : allHeaders) {
                builder.addHeaders(header.getKey(), header.getValue());
            }
        }

        // user agent
        if (!StringUtils.isNullOrEmpty(mRequestUserAgent)) {
            builder.addHeaders("User-Agent", mRequestUserAgent);
        }

        // prepare OkHttpClient

        OkHttpClient.Builder httpClientBuilder = getHttpClientBuilder();

        // http auth
        if (mHttpAuthEnabled) {
            logDebug("HTTP AUTH ENABLED");

            httpClientBuilder.authenticator(new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    String credential = Credentials.basic(mHttpAuthUsername, mHttpAuthPassword);
                    return response.request().newBuilder().header("Authorization", credential).build();
                }
            });
        }

        // SSL trusting for fake / self-generated certificates
        if (trustSSL) {
            logDebug("TRUST SSL enabled");

            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext;

            try {
                sslContext = SSLContext.getInstance("SSL");

                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                httpClientBuilder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                httpClientBuilder.hostnameVerifier(new HostnameVerifier() {
                    @SuppressLint("BadHostnameVerifier")
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // timeouts
        httpClientBuilder.connectTimeout(mConnectTimeout, TimeUnit.MILLISECONDS);
        httpClientBuilder.writeTimeout(mWriteTimeout, TimeUnit.MILLISECONDS);
        httpClientBuilder.readTimeout(mReadTimeout, TimeUnit.MILLISECONDS);

        builder.setOkHttpClient(httpClientBuilder.build());
        builder.setTag(objectTag);
        builder.setExecutor(mExecutor);

        // prep the request

        ANRequest request;

        if (requestIsMultipart) {
            request = ((ANRequest.MultiPartBuilder) builder).build();
        } else if (rt == RequestType.Post) {
            request = ((ANRequest.PostRequestBuilder) builder).build();
        } else if (rt == RequestType.Put) {
            request = ((ANRequest.PutRequestBuilder) builder).build();
        } else if (rt == RequestType.Delete) {
            request = ((ANRequest.DeleteRequestBuilder) builder).build();
        } else {
            request = ((ANRequest.GetRequestBuilder) builder).build();
        }

        onConnectionStart();

        if (synchronous) {
            try {
                ANResponse response = request.executeForJSONObject();

                if (response == null) {
                    onConnectionFailure(null);
                } else {
                    onConnectionSuccess(response.getOkHttpResponse(), (JSONObject) response.getResult());
                }

            } catch (Exception e) {
                e.printStackTrace();
                onConnectionFailure(null);
            } finally {
                onConnectionFinish();
            }
        } else {
            request.getAsOkHttpResponseAndJSONObject(new OkHttpResponseAndJSONObjectRequestListener() {
                @Override
                public void onResponse(Response okHttpResponse, JSONObject response) {
                    try {
                        onConnectionSuccess(okHttpResponse, response);
                    } finally {
                        onConnectionFinish();
                    }
                }

                @Override
                public void onError(ANError anError) {
                    try {
                        onConnectionFailure(anError);
                    } finally {
                        onConnectionFinish();
                    }
                }
            });
        }

        return true;
    }

    protected RequestParams filterRequestParams(RequestParams requestParams) {
        return requestParams;
    }

    protected String escapeHeaderValue(String headerValue) {
        if (headerValue == null || headerValue.length() < 1) {
            return "";
        }

        headerValue = headerValue.replace("\n", "");
        headerValue = headerValue.replace("\r", "");
        return headerValue;
    }

    public boolean makeCall() {
        boolean ret;
        try {
            ret = makeCall(true);
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        }

        if (!ret) {
            doPostOperations(false);
        }

        return ret;
    }

    public boolean startAsyncCall() {
        boolean ret;

        try {
            ret = makeCall(false);
        } catch (Exception e) {
            ret = false;
        }

        if (!ret) {
            doPostOperations(false);
        }

        return ret;
    }

    public boolean execute() {
        return startAsyncCall();
    }

    protected boolean makeCall(boolean synchronous) throws IOException {

        // override synchronous if looper not prepared
        if (Looper.myLooper() == null) {
            synchronous = true;
        }

        return executeInternal(synchronous, mServerHostname, mServerAddress, mUseSSL);
    }

    public boolean getIsCancelled() {
        return mIsCancelled;
    }

    /**
     * Wrong getter
     *
     * @deprecated use {@link #getIsCancelled()} instead.
     */
    @Deprecated
    public boolean isCancelled() {
        return getIsCancelled();
    }

    public void cancel(boolean interrupt) {
        if (!mIsCancelled && mIsBusy && objectTag != null) {
            mIsCancelled = true;
            log("Cancelling the request (" + mConnectionUrl + ")");
            AndroidNetworking.forceCancel(objectTag);
        }
    }

    public void cancel() {
        cancel(true);
    }

    public Context getContext() {
        return mContext;
    }

    public Logger getLogger() {
        return mLogger;
    }
}
