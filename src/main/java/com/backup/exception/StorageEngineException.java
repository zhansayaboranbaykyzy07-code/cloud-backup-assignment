package com.backup.exception;

public class StorageEngineException extends RuntimeException {
    public StorageEngineException(String message) {
        super(message);
    }

    public StorageEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
