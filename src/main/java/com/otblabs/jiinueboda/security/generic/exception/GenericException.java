package com.otblabs.jiinueboda.security.generic.exception;

import org.springframework.http.HttpStatus;

public class GenericException extends RuntimeException {

	private HttpStatus httpStatus;

	public GenericException(HttpStatus httpStatus, String message) {
		super(message);
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}
}
