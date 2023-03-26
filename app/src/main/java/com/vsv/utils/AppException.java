package com.vsv.utils;

public class AppException extends Exception {

    private final int messageId;

    public AppException(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }
}
