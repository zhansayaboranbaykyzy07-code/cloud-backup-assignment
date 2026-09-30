package com.backup.adapter;

import com.backup.driver.StorageDriver;
import com.backup.exception.StorageConnectionException;
import com.backup.exception.StorageEngineException;
import com.backup.exception.StorageQuotaExceededException;
import com.backup.legacy.LegacyFtpClient;
import com.backup.legacy.LegacyFtpException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class LegacyFtpStorageAdapter implements StorageDriver {

    private final LegacyFtpClient ftpClient;

    public LegacyFtpStorageAdapter(LegacyFtpClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    @Override
    public void store(String objectKey, byte[] content) {
        if (objectKey == null || content == null) {
            throw new StorageEngineException("Payload or key cannot be null");
        }

        InputStream stream = new ByteArrayInputStream(content);
        int resultCode = ftpClient.pushFile(objectKey, stream, content.length);

        if (resultCode == -101) {
            throw new StorageConnectionException("FTP connection error while storing " + objectKey);
        } else if (resultCode == -500) {
            throw new StorageQuotaExceededException("FTP quota exceeded for " + objectKey);
        } else if (resultCode != 0) {
            throw new StorageEngineException("FTP unknown error code: " + resultCode);
        }
    }

    @Override
    public byte[] retrieve(String objectKey) {
        LegacyFtpClient.FtpStatus status = new LegacyFtpClient.FtpStatus();
        byte[] data = ftpClient.fetchFile(objectKey, status);

        if (status.resultCode == 404 || data == null) {
            throw new StorageEngineException("File not found on FTP: " + objectKey);
        }
        if (status.resultCode != 200) {
            throw new StorageEngineException("FTP download error: " + status.statusMessage);
        }
        return data;
    }

    @Override
    public void remove(String objectKey) {
        try {
            boolean success = ftpClient.eraseFile(objectKey);
            if (!success) {
                throw new StorageEngineException("Failed to delete file on FTP: " + objectKey);
            }
        } catch (LegacyFtpException e) {
            throw new StorageEngineException("FTP deletion error: " + e.getMessage(), e);
        }
    }
}