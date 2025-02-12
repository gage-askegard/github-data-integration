package com.askegard.githubdataintegration.exceptions;

import feign.FeignException;

/**
 * Exception that signals an error occurred while making a service call
 */
public class ServiceCallException extends Exception {
    private final String errorBody;
    private final int statusCode;

    /**
     * Constructs a new instance from the given Feign exception
     *
     * @param feignException Feign exception from a failed service call to extract the error body and status code from
     */
    public ServiceCallException(FeignException feignException) {
        super(feignException);
        errorBody = feignException.contentUTF8();
        statusCode = feignException.status();
    }

    /**
     * Constructs a new instance
     *
     * @param message    Error message describing the issue
     * @param errorBody  Response body from the failed service call.
     * @param statusCode Status code of the failed service call.
     */
    public ServiceCallException(String message, String errorBody, int statusCode) {
        super(String.join("\n", message, "Response Code: " + statusCode, "Error: " + errorBody));
        this.errorBody = errorBody;
        this.statusCode = statusCode;
    }

    /**
     * @return the error body from the failed service call
     */
    public String getErrorBody() {
        return errorBody;
    }

    /**
     * @return the status code of the failed service call
     */
    public int getStatusCode() {
        return statusCode;
    }
}
