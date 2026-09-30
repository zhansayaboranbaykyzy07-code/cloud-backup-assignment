package com.backup.driver;

public interface StorageDriver {
    void store(String objectKey, byte[] content);
    byte[] retrieve(String objectKey);
    void remove(String objectKey);
}