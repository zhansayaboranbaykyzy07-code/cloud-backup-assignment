package com.backup.exception;

public class StorageQuotaExceededException extends StorageEngineException {
    public StorageQuotaExceededException(String message) {
        super(message);
    }
}