package com.backup.task;

import com.backup.driver.StorageDriver;

public abstract class BackupTask {
    protected final StorageDriver driver;

    public BackupTask(StorageDriver driver) {
        this.driver = driver;
    }

    public abstract void backup(String backupId, byte[] payload);
    public abstract byte[] restore(String backupId);
}