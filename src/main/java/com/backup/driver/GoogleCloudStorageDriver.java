package com.backup.driver;

import com.backup.exception.StorageEngineException;
import java.util.HashMap;
import java.util.Map;

public class GoogleCloudStorageDriver implements StorageDriver {
    private final Map<String, byte[]> gcsBucket = new HashMap<>();

    @Override
    public void store(String objectKey, byte[] content) {
        if (objectKey == null || content == null) {
            throw new StorageEngineException("GCS payload or key cannot be null");
        }
        gcsBucket.put(objectKey, content);
    }

    @Override
    public byte[] retrieve(String objectKey) {
        if (!gcsBucket.containsKey(objectKey)) {
            throw new StorageEngineException("Object not found in GCS: " + objectKey);
        }
        return gcsBucket.get(objectKey);
    }

    @Override
    public void remove(String objectKey) {
        gcsBucket.remove(objectKey);
    }
}