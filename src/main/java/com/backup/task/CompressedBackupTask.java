package com.backup.task;

import com.backup.driver.StorageDriver;

public class CompressedBackupTask extends BackupTask {

    private static final String PREFIX = "ZIP_HEADER:";

    public CompressedBackupTask(StorageDriver driver) {
        super(driver);
    }

    @Override
    public void backup(String backupId, byte[] payload) {
        String data = new String(payload);
        byte[] compressed = (PREFIX + data).getBytes();
        driver.store(backupId, compressed);
    }

    @Override
    public byte[] restore(String backupId) {
        byte[] raw = driver.retrieve(backupId);
        String str = new String(raw);
        if (str.startsWith(PREFIX)) {
            return str.substring(PREFIX.length()).getBytes();
        }
        return raw;
    }
}