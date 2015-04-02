package com.nimasystems.lightcast.network;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpRequest;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;
import com.nimasystems.lightcast.encryption.AESCrypt;
import com.nimasystems.lightcast.exceptions.InvalidParamsException;
import com.nimasystems.lightcast.utils.DebugUtils;
import com.nimasystems.lightcast.utils.StringUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

abstract public class ApiCallBase {

    private static final String DEFAULT_REQUEST_CHARSET = "UTF-8";
    public static final int DEFAULT_MAX_RETRIES = 3;

    public static final String XDG_HEADER_NAME = "X-Dg";
    public static final String XAT_HEADER_NAME = "X-At";
    public static final String XTZ_HEADER_NAME = "X-TZ";

    public static final int DEFAULT_CONNECT_TIMEOUT = 20000;
    protected int mConnectTimeout = DEFAULT_CONNECT_TIMEOUT;
    public static final int DEFAULT_READ_TIMEOUT = 20000;
    protected int mReadTimeout = DEFAULT_READ_TIMEOUT;
    public static final int DEFAULT_MAX_RETRIES_TIMEOUT = 20000;
    private static String mEd;
    protected final Logger mLogger = LoggerFactory.getLogger(this.getClass());
    protected AsyncHttpClient mAsyncHttpClient;
    protected RequestHandle mRequestHandle;
    protected List<Header> mRequestHeaders;
    protected String mConnectionUrl;
    protected String mRequestUserAgent;
    protected int mResponseStatusCode;
    protected List<Header> mResponseHeaders;
    protected byte[] mResponseBody;
    protected boolean mResponseIsSuccess;
    protected Context mContext;
    protected String mServerHostname;
    protected String mServerAddress;
    protected String mQueryPathPrefix;
    protected String mAccessToken;
    protected String mDeviceFriendlyName;
    protected String mDeviceGuid;
    protected String mLocale;
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
    private boolean mDebug;
    private String mConnectionId;
    private boolean mHttpAuthEnabled;
    private String mHttpAuthUsername;
    private String mHttpAuthPassword;
    private boolean mIsBusy;
    private TimeZone mServerTimezone;

    public ApiCallBase(Context context) throws InvalidParamsException {
        mContext = context;

        if (mContext == null) {
            throw new InvalidParamsException("Invalid context");
        }
    }

    public ApiCallBase(Context context, ApiCallTaskDelegate delegate) {
        mContext = context;
        mDelegate = delegate;

        mRequestHeaders = new ArrayList<>();
    }

    public static String encrDid(String deviceGuid, String deviceFriendlyName) {
        if (StringUtils.isNullOrEmpty(deviceGuid)) {
            return null;
        }

        String friendlyName = !StringUtils.isNullOrEmpty(deviceFriendlyName) ? deviceFriendlyName
                : "Emulator";
        String joined = deviceGuid + "|" + friendlyName + "|" + "android";

        String tkr;

        try {
            tkr = new AESCrypt().encrypt(joined);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return tkr;
    }

    protected static ApiServerErrorModel parseResponseForError(String response) {

        ApiServerErrorModel err;

        if (response != null) {

            try {
                JSONObject obj = new JSONObject(response);

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

                        // validation errors
                        JSONObject vJErrs = objError
                                .optJSONObject("validation_errors");
                        if (vJErrs != null) {
                            ArrayList<ApiServerValidationError> vErrs = new ArrayList<>();
                            ApiServerValidationError verr = new ApiServerValidationError();
                            String errorMessage = "\nValidation Errors\n\n";

                            Iterator<?> keys = vJErrs.keys();
                            while (keys.hasNext()) {
                                String key = (String) keys.next();
                                String error = vJErrs.optString(key);
                                errorMessage = errorMessage + key + ": "
                                        + error + "\n";
                            }

                            verr.message = errorMessage;
                            vErrs.add(verr);

                            err.validationErrors = vErrs;
                        }

                        return err;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    abstract protected boolean parseResponse(String response);

    abstract protected String getRequestQueryPath();

    public String getConnectionId() {
        return mConnectionId;
    }

    public void setSynchronizedCallback(boolean syncCallback) {
        mSynchronizedCallback = syncCallback;
    }

    protected ResponseHandlerInterface getResponseHandler() {
        return new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                onConnectionStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] responseBody) {
                onConnectionSuccess(statusCode, headers, responseBody);
            }

            @Override
            public void onFinish() {
                onConnectionFinish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] responseBody, Throwable error) {
                onConnectionFailure(statusCode, headers, responseBody, error);
            }

            @Override
            public void onCancel() {
                onConnectionCancel();
            }
        };
    }

    public String getRequestPreparedQueryPath() {
        String ret = null;
        String qp = getRequestQueryPath();

        if (qp != null) {
            ret = (!StringUtils.isNullOrEmpty(this.mQueryPathPrefix) ? this.mQueryPathPrefix
                    + qp
                    : qp);
        }
        return ret;
    }

    public String getHttpAuthUsername() {
        return mHttpAuthUsername;
    }

    public void setHttpAuthUsername(String username) {
        mHttpAuthUsername = username;
    }

    public String getHttpAuthPassword() {
        return mHttpAuthPassword;
    }

    public void setHttpAuthPassword(String password) {
        mHttpAuthPassword = password;
    }

    public boolean getHttpAuthEnabled() {
        return mHttpAuthEnabled;
    }

    public void setHttpAuthEnabled(boolean enabled) {
        mHttpAuthEnabled = enabled;
    }

    public boolean getSSLTrustAll() {
        return mSSLTrustAll;
    }

    public void setSSLTrustAll(boolean trustAll) {
        mSSLTrustAll = trustAll;
    }

    public String getRequestUserAgent() {
        return mRequestUserAgent;
    }

    public void setRequestUserAgent(String ua) {
        mRequestUserAgent = ua;
    }

    public boolean getDebug() {
        return mDebug;
    }

    public void setDebug(boolean debug) {
        mDebug = debug;
    }

    protected void log(String str) {
        mLogger.info(mConnectionId + ": " + str);
    }

    protected void logError(String str) {
        mLogger.error(mConnectionId + ": " + str);
    }

    protected void logDebug(String str) {
        mLogger.debug(mConnectionId + ": " + str);
    }

    /**
     * The query is constructed internally now.
     *
     * @deprecated use {@link #getRequestParams()} instead.
     */
    @Deprecated
    protected String getRequestQuery() {
        return null;
    }

    protected boolean initialize() {
        return true;
    }

    public void setConnectTimeout(int timeout) {
        mConnectTimeout = timeout;
    }

    public void setReadTimeout(int timeout) {
        mReadTimeout = timeout;
    }

    public TimeZone getServerTimezone() {
        return mServerTimezone;
    }

    public ApiServerErrorModel getServerError() {
        return mServerError;
    }

    public byte[] getResponseBody() {
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

    public void setUseSSL(boolean useSSL) {
        mUseSSL = useSSL;
    }

    public void setAccessToken(String accessToken) {
        mAccessToken = accessToken;
    }

    public void setServerHostname(String hostname) {
        mServerHostname = hostname;
    }

    public void setServerAddress(String address) {
        mServerAddress = address;
    }

    public void setQueryPathPrefix(String prefix) {
        mQueryPathPrefix = prefix;
    }

    public void setDeviceFriendlyName(String friendlyName) {
        mDeviceFriendlyName = friendlyName;
    }

    public void setDeviceGuid(String deviceGuid) {
        mDeviceGuid = deviceGuid;
    }

    public void setLocale(String locale) {
        mLocale = locale;
    }

    public boolean isSuccessful() {
        return mIsSuccessful;
    }

    protected boolean validateRequest() {
        return true;
    }

    protected boolean getIsPost() {
        return false;
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

    public RequestHandle getRequestHandle() {
        return mRequestHandle;
    }

    protected synchronized String getDeviceIdEncrypted() {
        String dId;

        // encrypt / hash the device id
        dId = mEd;

        if (dId == null && !StringUtils.isNullOrEmpty(mDeviceGuid)) {

            mEd = encrDid(mDeviceGuid, mDeviceFriendlyName);

            logDebug("Created X-Device: " + mEd);

            dId = mEd;
        }

        String dIdEnc = escapeHeaderValue(dId);

        return dIdEnc;
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
        mRequestHeaders.add(new BasicHeader("Charset", charset
                .toLowerCase(Locale.US)));

        // host
        if (!StringUtils.isNullOrEmpty(hostnameEnc)) {
            mRequestHeaders.add(new BasicHeader("Host", hostnameEnc));
        }

        // user agent
        if (!StringUtils.isNullOrEmpty(mRequestUserAgent)) {
            mRequestHeaders
                    .add(new BasicHeader("User-Agent", mRequestUserAgent));
        }

        // access token
        if (!StringUtils.isNullOrEmpty(accessToken)) {
            mRequestHeaders.add(new BasicHeader(XAT_HEADER_NAME, accessToken));
        }

        // device id
        String dIdEnc = getDeviceIdEncrypted();

        if (!StringUtils.isNullOrEmpty(dIdEnc)) {
            mRequestHeaders.add(new BasicHeader(XDG_HEADER_NAME, dIdEnc));
        }

        // accept language fixing
        String ac = mLocale;
        ac = (ac != null) ? ac.replace('_', '-') : null;

        if (!StringUtils.isNullOrEmpty(ac)) {
            mRequestHeaders.add(new BasicHeader("Accept-Language", ac != null ? ac
                    .toLowerCase(Locale.US) : null));
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

    protected void onConnectionSuccess(int statusCode, Header[] headers,
                                       byte[] responseBody) {
        mResponseStatusCode = statusCode;
        mResponseHeaders = makeResponseHeaders(headers);
        mResponseBody = responseBody;
        mResponseIsSuccess = true;

        logDebug("Connection success (" + mConnectionUrl + "), Status code: "
                + statusCode);

        doPostOperations();
    }

    protected void onConnectionFailure(int statusCode, Header[] headers,
                                       byte[] responseBody, @SuppressWarnings("UnusedParameters") Throwable error) {
        mResponseStatusCode = statusCode;
        mResponseHeaders = makeResponseHeaders(headers);
        mResponseBody = responseBody;
        mResponseIsSuccess = false;

        logDebug("Connection failure (" + mConnectionUrl + "), Status code: "
                + statusCode);

        doPostOperations();
    }

    protected List<Header> makeResponseHeaders(Header[] headers) {
        if (headers == null || headers.length < 1) {
            return null;
        }

        int i;

        List<Header> allh = new ArrayList<>();

        for (i = 0; i <= headers.length - 1; i++) {
            allh.add(headers[i]);
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
            n = ah.getName();
            if (n.equalsIgnoreCase(name)) {
                h = ah;
                break;
            }
        }

        return h;
    }

    public String getResponseHeaderValue(String name) {
        Header header = getResponseHeader(name);
        String ret = (header != null ? header.getValue() : null);
        return ret;
    }

    protected void parseResponseHeaders() {

        if (mResponseHeaders == null) {
            return;
        }

        // timezone
        String tzCode = getResponseHeaderValue(XTZ_HEADER_NAME);

        if (!StringUtils.isNullOrEmpty(tzCode)) {
            mServerTimezone = TimeZone.getTimeZone(tzCode);
        }
    }

    protected void doPostOperations() {
        parseResponseHeaders();
        parseResponse();

        mIsBusy = false;

        // notify the delegate
        if (mDelegate != null) {
            if (!mSynchronizedCallback && (mIsSynchronous || isOnMainThread())) {
                if (mIsSuccessful) {
                    mDelegate.didFinishTask(this);
                } else {
                    mDelegate.didFinishTaskWithError(this, mLastErrorCode,
                            mLastErrorMessage, mServerError);
                }
            } else {
                Handler mainHandler = new Handler(mContext.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    public void run() {
                        if (mIsSuccessful) {
                            mDelegate.didFinishTask(ApiCallBase.this);
                        } else {
                            mDelegate.didFinishTaskWithError(ApiCallBase.this,
                                    mLastErrorCode, mLastErrorMessage,
                                    mServerError);
                        }
                    }
                };
                mainHandler.post(myRunnable);
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
        boolean t = (Looper.myLooper() == Looper.getMainLooper());
        return t;
    }

    protected void parseResponse() {

        try {
            String result = (mResponseBody != null ? new String(mResponseBody,
                    "UTF-8") : null);

            logDebug("HTTP Response: " + result);

            // parse for a server error
            mServerError = parseResponseForError(result);

            if (mServerError != null) {
                mIsSuccessful = false;
                mLastErrorCode = mServerError.code;
                mLastErrorMessage = mServerError.message;
            } else {
                mIsSuccessful = parseResponse(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logError("Unhandled exception while parsing the response: " + e);

            mLastErrorMessage = e.getMessage();
        }
    }

    protected String getRequestContentType() {
        return null;
    }

    protected HttpEntity getRequestEntity() {
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
        String url = (useSSL ? "https://" : "http://")
                + (!StringUtils.isNullOrEmpty(serverAddress) ? serverAddress
                : serverHostname)
                + (!StringUtils.isNullOrEmpty(queryPath) ? queryPath : "")
                + (!StringUtils.isNullOrEmpty(requestQuery) ? "?"
                + requestQuery : "");
        return url;
    }

    protected boolean executeInternal(boolean synchronous,
                                      String serverHostname, String serverAddress, boolean useSSL)
            throws IOException {

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

        boolean isPostRequest = getIsPost();

        HttpEntity httpE = getRequestEntity();
        String queryPath = getRequestPreparedQueryPath();
        @SuppressWarnings("deprecation") String requestQueryGet = (!isPostRequest ? getRequestQuery() : null);
        @SuppressWarnings("deprecation") String requestQueryPost = (isPostRequest ? getRequestQuery() : null);

        if (StringUtils.isNullOrEmpty(queryPath)) {
            if (mDebug) {
                DebugUtils.ass(false, "invalid query path");
            }
            return false;
        }

        // prepare the url
        mConnectionUrl = getConnectionUrl(useSSL, serverAddress,
                serverHostname, queryPath, requestQueryGet);

        logDebug("URL: " + mConnectionUrl);

        String charset = getRequestCharset();

        // http://stackoverflow.com/questions/2172752/httpsurlconnection-and-intermittent-connections
        if (useSSL || Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            disableConnectionReuseIfNecessary();
        }

        // mIsBusy
        mAsyncHttpClient = null;

        boolean trustSSL = (!mUseSSL || mSSLTrustAll);

        if (synchronous) {
            //noinspection EmptyMethod
            mAsyncHttpClient = new SyncHttpClient(trustSSL, 80, 443) {

                @Override
                protected AsyncHttpRequest newAsyncHttpRequest(
                        DefaultHttpClient client, HttpContext httpContext,
                        HttpUriRequest uriRequest, String contentType,
                        ResponseHandlerInterface responseHandler,
                        Context context) {
                    return super.newAsyncHttpRequest(client, httpContext,
                            uriRequest, contentType, responseHandler, context);
                }
            };
        } else {
            //noinspection EmptyMethod
            mAsyncHttpClient = new AsyncHttpClient(trustSSL, 80, 443) {

                @Override
                protected AsyncHttpRequest newAsyncHttpRequest(
                        DefaultHttpClient client, HttpContext httpContext,
                        HttpUriRequest uriRequest, String contentType,
                        ResponseHandlerInterface responseHandler,
                        Context context) {
                    return super.newAsyncHttpRequest(client, httpContext,
                            uriRequest, contentType, responseHandler, context);
                }
            };
        }

        // http auth
        if (mHttpAuthEnabled) {
            mAsyncHttpClient.setBasicAuth(mHttpAuthUsername, mHttpAuthPassword);
        }

        mAsyncHttpClient.setTimeout(mConnectTimeout);

        if (!StringUtils.isNullOrEmpty(mRequestUserAgent)) {
            mAsyncHttpClient.setUserAgent(mRequestUserAgent);
        }

        mAsyncHttpClient.setMaxRetriesAndTimeout(getRequestMaxRetries(),
                getRequestMaxRetriesTimeout());

        // mAsyncHttpClient.setProxy("127.0.0.1", 8888);

        if (isPostRequest && httpE == null
                && !StringUtils.isNullOrEmpty(requestQueryPost)) {
            mRequestHeaders.add(new BasicHeader("Content-Type",
                    "application/x-www-form-urlencoded; charset=" + charset));
        }

        prepareMyHeaders(serverHostname, charset);

        Header[] allHeaders = getAllHeaders();

        if (isPostRequest) {
            if (httpE == null && !StringUtils.isNullOrEmpty(requestQueryPost)) {
                // check the deprecated requestQuery
                httpE = new StringEntity(requestQueryPost);
            }

            if (httpE != null) {
                mRequestHandle = mAsyncHttpClient.post(mContext,
                        mConnectionUrl, allHeaders, httpE,
                        getRequestContentType(), getResponseHandler());
            } else {
                mRequestHandle = mAsyncHttpClient.post(mContext,
                        mConnectionUrl, allHeaders, getRequestParams(),
                        getRequestContentType(), getResponseHandler());
            }
        } else {
            mRequestHandle = mAsyncHttpClient.get(mContext, mConnectionUrl,
                    allHeaders, getRequestParams(), getResponseHandler());
        }

        return true;
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
            doPostOperations();
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
            doPostOperations();
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
        if (!mIsCancelled && mIsBusy && mRequestHandle != null) {
            mIsCancelled = true;
            log("Cancelling the request (" + mConnectionUrl + ")");
            mRequestHandle.cancel(interrupt);
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
