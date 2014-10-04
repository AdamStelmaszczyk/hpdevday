package com.hp.sloiki.network;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class NetworkService {

    private static final String LOG_TAG = "NETWORK";

    private static final int STATUS_OK = 200;
    private static final int STATUS_NO_ACCESS = 400;
    private static final int STATUS_SERVER_ERROR = 500;

    private static NetworkService instance = null;
    private static HttpContext httpContext;

    protected NetworkService() {
        CookieStore cookieStore = new BasicCookieStore();
        httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    }

    public synchronized static NetworkService getInstance() {
        if (instance == null) {
            instance = new NetworkService();
        }
        return instance;
    }

    private String readData(HttpResponse response) {
        if (response == null) {
            return null;
        }

        BufferedReader in;
        StringBuilder builder = new StringBuilder();

        try {
            in = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                builder.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    /**
     * Handles error and returns true if everything went fine
     * @param response
     * @param url
     * @return
     * @throws NetworkServiceException
     */
    private boolean checkResponseStatus(HttpResponse response, String url) throws NetworkServiceException {
        Integer responseCode = response.getStatusLine().getStatusCode();
        Log.d(LOG_TAG, "Request to " + url + " completed with status code: " + responseCode.toString());
        if (responseCode == STATUS_OK) {
            return true;
        } else if (responseCode >= STATUS_NO_ACCESS && responseCode < STATUS_SERVER_ERROR) {
            throw new NetworkServiceException(url, NetworkServiceException.NetworkFailureReason.BAD_REQUEST, responseCode);
        } else if (responseCode >= STATUS_SERVER_ERROR) {
            throw new NetworkServiceException(url, NetworkServiceException.NetworkFailureReason.INTERNAL_SERVER_ERROR, responseCode);
        } else {
            throw new NetworkServiceException(url, NetworkServiceException.NetworkFailureReason.UNKNOWN_ERROR, responseCode);
        }
    }

    private HttpResponse execute(HttpUriRequest request) throws NetworkServiceException {
        Log.d(LOG_TAG, "Executing " + request.getMethod() + " to " + request.getURI().toString());
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = null;
        try {
            response = httpClient.execute(request, httpContext);
        } catch (SocketTimeoutException e) {
            Log.w(LOG_TAG, "Service timeout");
            throw new NetworkServiceException(request.getURI().toString(), NetworkServiceException.NetworkFailureReason.TIMEOUT, -1);
        } catch (IOException e) {
            e.printStackTrace();
            throw new NetworkServiceException(request.getURI().toString(), NetworkServiceException.NetworkFailureReason.UNKNOWN_ERROR, -1);
        }
        return response;
    }

    public Boolean authenticate(String url, String user, String password) throws NetworkServiceException {
        Log.d(LOG_TAG, "Auth(POST) to " + url);
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
        nameValuePair.add(new BasicNameValuePair("username", user));
        nameValuePair.add(new BasicNameValuePair("password", password));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpResponse response = this.execute(httpPost);
        return this.checkResponseStatus(response, url);
    }

    public String sendGET(String url) throws NetworkServiceException {
        Log.d(LOG_TAG, "GET to " + url);

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Content-Type", "application/json");
        HttpResponse response = this.execute(httpGet);
        this.checkResponseStatus(response, url);

        String responseString = this.readData(response);
        if (responseString != null) {
            String shortResponse = responseString;
            if (responseString.length() > 300) {
                shortResponse = responseString.substring(0, 299);
            }
            Log.d(LOG_TAG, "Response: " + shortResponse);
        } else {
            Log.d(LOG_TAG, "Empty response");
        }
        return responseString;
    }

    public Boolean sendPOST(String url, String json) throws NetworkServiceException {
        //TODO: implement me!
        return false;
    }

    public Boolean sendPUT(String url, String json) throws NetworkServiceException {
        Log.d(LOG_TAG, "PUT to " + url + " json: " + json);

        HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader("Content-Type", "application/json");

        try {
            httpPut.setEntity(new StringEntity(json));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpResponse    response = this.execute(httpPut);
        return this.checkResponseStatus(response, url);
    }

    public Boolean sendDELETE(String url, Integer id) throws NetworkServiceException {
        Log.d(LOG_TAG, "DELETE to " + url + " id: " + id);

        if (id != null) {
            url += "/" + id.toString();
        }
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.addHeader("Content-Type", "application/json");


        HttpResponse response = this.execute(httpDelete);
        return this.checkResponseStatus(response, url);
    }
}
