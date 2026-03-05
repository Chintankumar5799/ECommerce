package com.example.demo.auth.exception;


public class ErrorResponse {
	    private String message;
	    private int status;
	    private long timestamp;

	    // Constructors, getters, and setters
	    public ErrorResponse(String message, int status, long timestamp) {
	        this.message = message;
	        this.status = status;
	        this.timestamp = timestamp;
	    }
	    // Getters...

		public String getMessage() {
			return message;
		}

		public int getStatus() {
			return status;
		}

		public long getTimestamp() {
			return timestamp;
		}
	

}
