# Design Rationale Document

## 1. Problem Domain
Cloud Storage & Data Backup Platform. The system manages data backups (encrypting or compressing payload) and saves results to cloud storage providers (AWS S3, Google Cloud Storage, Legacy FTP).

## 2. Why Neither Pattern Alone Is Sufficient
* **Why Bridge alone is not enough**: Bridge decouples the abstraction hierarchy (`BackupTask`) from the implementation hierarchy (`StorageDriver`). However, `LegacyFtpClient` has a completely incompatible API (uses streams, returns status codes like -101/-500, uses mutable status objects). Without Adapter, we would need to pollute the clean `StorageDriver` interface or alter third-party code.
* **Why Adapter alone is not enough**: Adapter only solves the incompatibility of `LegacyFtpClient`. Without Bridge, adding new types of backup tasks (e.g., Encrypted, Compressed) and storage backends (S3, GCS, FTP) would cause a combinatorial class explosion.

## 3. Incompatibility of the Adapted Class
`LegacyFtpClient` is genuinely incompatible:
1. **Method Signatures**: `pushFile`, `fetchFile`, `eraseFile` instead of `store`, `retrieve`, `remove`.
2. **Parameters**: Accepts `InputStream` and length `int` instead of `byte[]`.
3. **Error Handling**: Returns numeric status codes (`-101`, `-500`) and uses `FtpStatus` objects instead of domain exceptions.
4. **Exceptions**: Throws checked `LegacyFtpException` on deletion.

`LegacyFtpStorageAdapter` wraps these features and translates all failures into domain exceptions (`StorageConnectionException`, `StorageQuotaExceededException`, `StorageEngineException`).

## 4. Required Complexity Module
**Dynamic Implementor Selection** (`StorageDriverRegistry`). Drivers are selected at runtime based on the URI scheme (`s3://`, `gcs://`, `ftp://`).

## 5. System Limitation
`StorageDriver` processes byte arrays in memory (`byte[]`). Large backups (gigabytes) will consume significant RAM. A stream-based interface would be needed for production scalability.
