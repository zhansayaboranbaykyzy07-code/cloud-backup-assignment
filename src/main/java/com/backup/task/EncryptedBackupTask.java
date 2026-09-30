package com.backup.task;

import com.backup.driver.StorageDriver;
import java.util.Base64;

public class EncryptedBackupTask extends BackupTask {

    public EncryptedBackupTask(StorageDriver driver) {
        super(driver);
    }

    @Override
    public void backup(String backupId, byte[] payload) {
        byte[] encrypted = Base64.getEncoder().encode(payload);
        driver.store(backupId, encrypted);
    }

    @Override
    public byte[] restore(String backupId) {
        byte[] raw = driver.retrieve(backupId);
        return Base64.getDecoder().decode(raw);
    }
}