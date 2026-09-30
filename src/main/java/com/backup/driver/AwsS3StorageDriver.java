package com.backup.driver;

import com.backup.exception.StorageEngineException;
import java.util.HashMap;
import java.util.Map;

public class AwsS3StorageDriver implements StorageDriver {
    private final Map<String, byte[]> s3Bucket = new HashMap<>();

    @Override
    public void store(String objectKey, byte[] content) {
        if (objectKey == null || content == null) {
            throw new StorageEngineException("S3 payload or key cannot be null");
        }
        s3Bucket.put(objectKey, content);
    }

    @Override
    public byte[] retrieve(String objectKey) {
        if (!s3Bucket.containsKey(objectKey)) {
            throw new StorageEngineException("Object not found in S3: " + objectKey);
        }
        return s3Bucket.get(objectKey);
    }

    @Override
    public void remove(String objectKey) {
        s3Bucket.remove(objectKey);
    }
}