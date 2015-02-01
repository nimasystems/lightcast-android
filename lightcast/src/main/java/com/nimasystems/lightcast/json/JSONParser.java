package com.nimasystems.lightcast.json;

import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class JSONParser {

    public static String getJSONfromURL(String url) {
        String result = "";
        String charset = "UTF-8";
        InputStream response;
        HttpURLConnection connection = null;
        URL newUrl = null;
        String line;

        try {
            newUrl = new URL(url);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }

        try {
            try {
                if (newUrl.getProtocol().toLowerCase().equals("http")) {
                    // SSLSocketFactory sslSocketFactory = trustAllHosts(url);
                    connection = (HttpURLConnection) new URL(url)
                            .openConnection();
                    connection.setReadTimeout(1000);
                    // connection.setSSLSocketFactory( sslSocketFactory );
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Charset", charset);
                    connection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded;charset="
                                    + charset);
                }
            } catch (ClientProtocolException e) {
                Log.e("log_tag", "ClientProtocol Exception: " + e.toString());
                e.printStackTrace();
            }

            response = new BufferedInputStream(connection.getInputStream());
            Log.i("response", "response : " + response);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response, "UTF-8"), 8);
            Log.i("reader", "reader : " + reader);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            response.close();
            result = sb.toString();

            connection.disconnect();

        } catch (Exception e) {
            Log.e("log_tag", e.toString());
            e.printStackTrace();
        }

        Log.i("result", "result : " + result);
        return result;
    }

    private static SSLSocketFactory trustAllHosts(String url) {
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(final X509Certificate[] chain,
                                           final String authType) {
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain,
                                           final String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts,
                    new java.security.SecureRandom());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Create an ssl socket factory with our all-trusting manager
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        if (url.startsWith("https://")) {
            HttpsURLConnection
                    .setDefaultHostnameVerifier(new HostnameVerifier() {
                        public boolean verify(String hostname,
                                              SSLSession session) {
                            return true;
                        }
                    });
        }
        return sslSocketFactory;
    }

    public static String getJSONfromURL_SSL(String url) {
        String result = "";
        String charset = "UTF-8";
        InputStream response;
        HttpsURLConnection connection = null;
        URL newUrl = null;
        String line;

        try {
            newUrl = new URL(url);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }

        try {
            try {
                if (newUrl.getProtocol().toLowerCase().equals("https")) {
                    SSLSocketFactory sslSocketFactory = trustAllHosts(url);
                    connection = (HttpsURLConnection) new URL(url)
                            .openConnection();
                    connection.setReadTimeout(1000);
                    connection.setSSLSocketFactory(sslSocketFactory);
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Charset", charset);
                    connection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded;charset="
                                    + charset);
                }
            } catch (ClientProtocolException e) {
                Log.e("log_tag", "ClientProtocol Exception: " + e.toString());
                e.printStackTrace();
            }

            response = new BufferedInputStream(connection.getInputStream());
            Log.i("response", "response : " + response);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    response, "UTF-8"), 8);
            Log.i("reader", "reader : " + reader);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            response.close();
            result = sb.toString();

            connection.disconnect();

        } catch (Exception e) {
            Log.e("log_tag", e.toString());
            e.printStackTrace();
        }

        Log.i("result", "result : " + result);
        return result;
    }

    public static String getJSONfromURLRegister_SSL(String url, String params) {
        String result = "";
        String charset = "UTF-8";
        HttpsURLConnection connection = null;
        URL newUrl = null;

        try {
            // newUrl = new URL(url);
            newUrl = new URL(url.replace(' ', '+'));
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        }

        try {
            try {
                if (newUrl.getProtocol().toLowerCase().equals("https")) {
                    SSLSocketFactory sslSocketFactory = trustAllHosts(url);
                    connection = (HttpsURLConnection) newUrl.openConnection();
                    connection.setDoOutput(true);
                    connection.setSSLSocketFactory(sslSocketFactory);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Charset", charset);
                    connection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded;charset="
                                    + charset);
                    OutputStream output = null;
                    try {
                        output = connection.getOutputStream();
                        output.write(params.getBytes(charset));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (output != null)
                            try {
                                output.close();
                            } catch (IOException logOrIgnore) {
                                logOrIgnore.printStackTrace();
                            }
                    }
                }
            } catch (ClientProtocolException e) {
                Log.e("log_tag", "ClientProtocol Exception: " + e.toString());
            }

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");

            }
            result = sb.toString();

            connection.disconnect();

        } catch (Exception e) {
            Log.e("log_tag", e.toString());
        }

		/*
         * try{ jArray = new JSONObject(result); }catch(JSONException e){
		 * Log.e("log_tag",e.toString()); } return jArray;
		 */
        Log.i("result", "result : " + result);
        return result;

    }

    public static JSONObject getJSONfromURLRegister(String url, String params) {
        String result = "";
        JSONObject jArray = null;
        String charset = "UTF-8";
        // InputStream response = null;
        HttpURLConnection connection = null;
        URL newUrl = null;

        try {
            // newUrl = new URL(url);
            newUrl = new URL(url.replace(' ', '+'));
        } catch (MalformedURLException e1) {
            Log.i("log_tag", "URL Exception : " + e1.toString());
            e1.printStackTrace();
        }

        try {
            try {
                if (newUrl.getProtocol().toLowerCase().equals("http")) {
                    connection = (HttpURLConnection) newUrl.openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Charset", charset);
                    connection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded;charset="
                                    + charset);
                    OutputStream output = null;
                    try {
                        output = connection.getOutputStream();
                        output.write(params.getBytes(charset));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (output != null)
                            try {
                                output.close();
                            } catch (IOException logOrIgnore) {
                                logOrIgnore.printStackTrace();
                            }
                    }
                }
            } catch (ClientProtocolException e) {
                Log.e("log_tag", "ClientProtocol Exception: " + e.toString());
            }

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");

            }
            result = sb.toString();

            connection.disconnect();

        } catch (Exception e) {
            Log.e("log_tag", e.toString());
        }

        try {
            jArray = new JSONObject(result);
        } catch (JSONException e) {
            Log.e("log_tag", e.toString());
        }
        Log.i("jArray", "jArray : " + jArray);
        return jArray;

    }

}
