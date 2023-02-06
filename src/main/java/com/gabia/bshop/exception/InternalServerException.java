package com.gabia.bshop.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends CustomException {

    public InternalServerException(final String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
