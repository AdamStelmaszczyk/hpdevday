package com.hp.sloiki.network;


public class NetworkServiceException extends Exception {

    public enum NetworkFailureReason {
        TIMEOUT,
        BAD_REQUEST,
        UNKNOWN_ERROR,
        INTERNAL_SERVER_ERROR
    }

    private String url;
    private NetworkFailureReason reason;
    private Integer responseCode;

    NetworkServiceException(String url, NetworkFailureReason reason, Integer responseCode) {
        this.url = url;
        this.reason = reason;
        this.responseCode = responseCode;
    }

    public NetworkFailureReason getReason() {
        return reason;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String getMessage() {
        return "Request to " + this.url + " failed with reason: " + this.reason.toString() + "("+ responseCode.toString() +").";
    }
}
