# FileService

A Java-based file system service accessible through an HTTP JSON-RPC 2.0 endpoint.

---

## Features

- Retrieve file information (name, path, size)
- List contents of directories
- Create files and folders
- Delete files and folders
- Move and copy files or directories
- Append data to files with concurrency isolation
- Read a specified number of bytes from a file with offset

---

## File System Operations

This section outlines the behavior and guarantees of each supported operation:

### `getFileInfo`

- **Input**: `path` (String)
- **Returns**:
    - `name`: file or directory name
    - `path`: relative path
    - `size`: file size in bytes (0 for directories)
    - `directory`: whether it is a directory
- **Errors**:
    - If the file does not exist
    - If the path escapes the root

---

### `listChildren`

- **Input**: `path` (String)
- **Returns**:
    - Array of `FileInfo` objects for all immediate children
- **Errors**:
    - If the path does not exist
    - If the path is not a directory
    - If the path escapes the root

---

### `create`

- **Input**:
    - `path` (String)
    - `directory` (Boolean)
- **Behavior**:
    - Creates an empty file or directory
    - Parent directories are created automatically for files
- **Errors**:
    - If the target already exists
    - If the path is empty or escapes the root

---

### `delete`

- **Input**: `path` (String)
- **Behavior**:
    - Deletes a file or directory recursively
- **Errors**:
    - If the path does not exist
    - If the path is invalid or escapes the root

---

### `move`

- **Input**:
    - `source` (String)
    - `destination` (String)
- **Behavior**:
    - Moves a file or directory
    - Overwrites files at destination
    - Fails if destination is a non-empty directory
- **Errors**:
    - If the source does not exist
    - If either path is invalid or escapes the root

---

### `copy`

- **Input**:
    - `source` (String)
    - `destination` (String)
- **Behavior**:
    - Copies files or directories recursively
    - Overwrites files at destination
    - Fails if destination is a non-empty directory
- **Errors**:
    - If the source does not exist
    - If either path is invalid or escapes the root

---

### `append`

- **Input**:
    - `path` (String)
    - `data` (String)
- **Behavior**:
    - Appends text to an existing file
    - Isolated using per-file concurrency locking
- **Errors**:
    - If the file does not exist
    - If the path is a directory or escapes the root

#### Concurrency Isolation for Append

The `append` operation guarantees isolation across concurrent clients:

- Each file has a dedicated lock managed by the `FileConcurrencyManager`.
- The locking mechanism ensures that only one thread can write to a file at a time.
- Other threads trying to append to the same file will block until the lock is released.
- Locks are stored in a concurrent map (`ConcurrentHashMap<Path, ReentrantLock>`) and created lazily.

---

### `read`

- **Input**:
    - `path` (String)
    - `offset` (int)
    - `length` (int)
- **Returns**:
    - Data starting from offset
- **Behavior**:
    - Returns N bytes from a file at a specific offset. Return less than N bytes if EOF is reached.
    - Returns empty string if offset is beyond end of file
- **Errors**:
    - If the file does not exist or is a directory
    - If the path is invalid or escapes the root

---

## Technology Stack

- Java 17 + Spring Boot
- JSON-RPC 2.0
- JUnit 5 for unit & integration tests
- Docker for containerization
- Helm for Kubernetes deployment

---

## Configuration

The root directory is configurable and set in the ```src/main/resources/application.yaml``` file.

```yaml
# application.yaml
root:
  path: /data/fileservice
```

---

## Testing

Unit and integration test coverage for all the operations required.


---

## Build & Run

### Run Locally

Open the project in IntelliJ 😊 and run the ```FileServiceApplication``` file.

### Run via Docker

```
docker build -t fileservice .
docker run -p 8080:8080 -e ROOT_PATH=/data -v $(pwd)/data:/data fileservice
```

### Run via Helm (Kubernetes)

```
minikube start
helm install fileservice ./charts/fileservice
kubectl port-forward svc/fileservice 8080:8080
```

### Persistent Storage

The Helm chart creates a persistent volume ensuring the data survives application restarts. The default storage location
is ```/data/fileservice```.

---

## Usage

### Endpoint

```
POST /filesystem
Content-Type: application/json
```

All the operations map the request parameters to a DTO. The request classes are at ```rpc/dto/request``` .

### Example: Create a Directory

```json
{
  "jsonrpc": "2.0",
  "method": "create",
  "params": [
    {
      "path": "example",
      "directory": true
    }
  ],
  "id": 1
}
```

### Postman

A Postman collection containing all supported operations is available in the ```postman``` directory.

---

## Design Decisions & Tradeoffs

- **Validation is centralized** in `ValidationUtils` and applied in service layer for all write operations.
- **Concurrency control** is file-scoped, and managed through a ```FileConcurrencyManager```.
    - The implement concurrency assumes that only one instance will be running the service.
- **IO access** is done through a ```FileIOManager```.
- DTOs were used for clear request structure and extensibility.
    - DTOs include validation annotations for clarity and potential future extensibility. However, due to limitations in
      jsonrpc4j, these annotations are not currently enforced.
- **String Append** data in append is treated as UTF-8 strings for simplicity; can be extended to support binary.