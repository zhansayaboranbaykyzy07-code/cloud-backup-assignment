package com.backup;

import com.backup.adapter.LegacyFtpStorageAdapter;
import com.backup.driver.StorageDriver;
import com.backup.exception.StorageConnectionException;
import com.backup.exception.StorageEngineException;
import com.backup.exception.StorageQuotaExceededException;
import com.backup.legacy.LegacyFtpClient;
import com.backup.legacy.LegacyFtpException;
import com.backup.task.CompressedBackupTask;
import com.backup.task.EncryptedBackupTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackupPlatformTest {

    private StorageDriver mockDriver;
    private LegacyFtpClient mockFtpClient;
    private LegacyFtpStorageAdapter ftpAdapter;

    @BeforeEach
    void setUp() {
        mockDriver = mock(StorageDriver.class);
        mockFtpClient = mock(LegacyFtpClient.class);
        ftpAdapter = new LegacyFtpStorageAdapter(mockFtpClient);
    }

    @Test
    @DisplayName("EncryptedBackupTask должна шифровать и передавать данные в StorageDriver")
    void testEncryptedBackupTaskDelegation() {
        EncryptedBackupTask task = new EncryptedBackupTask(mockDriver);
        byte[] payload = "MySecretData".getBytes();
        String backupId = "backup-101";

        task.backup(backupId, payload);

        byte[] expectedEncrypted = Base64.getEncoder().encode(payload);
        verify(mockDriver, times(1)).store(eq(backupId), eq(expectedEncrypted));
    }

    @Test
    @DisplayName("CompressedBackupTask должна сжимать и восстанавливать данные")
    void testCompressedBackupTaskDelegation() {
        CompressedBackupTask task = new CompressedBackupTask(mockDriver);
        byte[] payload = "LogDataStream".getBytes();
        String backupId = "backup-102";
        byte[] compressedData = ("ZIP_HEADER:" + "LogDataStream").getBytes();

        when(mockDriver.retrieve(backupId)).thenReturn(compressedData);

        task.backup(backupId, payload);
        verify(mockDriver, times(1)).store(eq(backupId), eq(compressedData));

        byte[] restored = task.restore(backupId);
        assertArrayEquals(payload, restored);
    }

    @Test
    @DisplayName("Адаптер транслирует ошибку сети (-101) в StorageConnectionException")
    void testAdapterConnectionExceptionTranslation() {
        when(mockFtpClient.pushFile(anyString(), any(InputStream.class), anyInt())).thenReturn(-101);

        assertThrows(StorageConnectionException.class, () -> {
            ftpAdapter.store("network_error.txt", "payload".getBytes());
        });
    }

    @Test
    @DisplayName("Адаптер транслирует переполнение диска (-500) в StorageQuotaExceededException")
    void testAdapterQuotaExceededExceptionTranslation() {
        when(mockFtpClient.pushFile(anyString(), any(InputStream.class), anyInt())).thenReturn(-500);

        assertThrows(StorageQuotaExceededException.class, () -> {
            ftpAdapter.store("large_file.iso", new byte[11_000_000]);
        });
    }

    @Test
    @DisplayName("Адаптер перехватывает LegacyFtpException при удалении и оборачивает в StorageEngineException")
    void testAdapterDeletionExceptionTranslation() throws LegacyFtpException {
        when(mockFtpClient.eraseFile("protected.txt")).thenThrow(new LegacyFtpException("Permission denied"));

        StorageEngineException ex = assertThrows(StorageEngineException.class, () -> {
            ftpAdapter.remove("protected.txt");
        });

        assertTrue(ex.getMessage().contains("FTP deletion error"));
    }
}