package com.backup.legacy;

import java.io.InputStream;

public class LegacyFtpClient {

    public static class FtpStatus {
        public int resultCode = 0;
        public String statusMessage = "OK";
    }

    public int pushFile(String remotePath, InputStream stream, int length) {
        if (length > 10_000_000) {
            return -500;
        }
        if (remotePath.contains("network_error")) {
            return -101;
        }
        return 0;
    }

    public byte[] fetchFile(String remotePath, FtpStatus status) {
        if (remotePath.contains("missing")) {
            status.resultCode = 404;
            status.statusMessage = "File Not Found";
            return null;
        }
        status.resultCode = 200;
        return ("FTP_RAW_DATA:" + remotePath).getBytes();
    }

    public boolean eraseFile(String remotePath) throws LegacyFtpException {
        if (remotePath.contains("protected")) {
            throw new LegacyFtpException("FTP-503: Permission denied");
        }
        return true;
    }
}
