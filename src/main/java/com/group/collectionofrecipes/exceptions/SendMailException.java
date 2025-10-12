package com.group.collectionofrecipes.exceptions;

public class SendMailException extends RuntimeException {
    public SendMailException(String message) {
        super(message);
    }
}
