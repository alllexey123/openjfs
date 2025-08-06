# Open Java File Server (🛠️ WIP 🛠️)

A simple, open-source **Java Spring Boot** application designed to serve static files from a specified directory. It provides straightforward, unauthenticated access to your files, for easy personal or internal network file sharing.

**Please note: This project is under active development. Expect changes and potential instability.**

**Symlinks are not officially supported at the moment.**
## Features

*   **Directory Download (as ZIP):** Download entire directories as a single ZIP archive.
*   **Direct File Download:** Access and download individual files directly through a simple URL structure.
*   **JSON Directory Listings and Search:** Search or list your files (with some basic data) using simple API.

## Roadmap

*   [x] **JSON Directory Listings**
*   [x] **Search**
*   [ ] **Indexed Search** *(maybe)*
*   [ ] **Web UI** 
*   [ ] **Admin Panel**
*   [ ] **Docker Support**

## Getting Started

### Prerequisites

*   JDK 21 or higher
*   Apache Maven

### Installation and Execution

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/alllexey123/openjfs
    cd openjfs
    ```

2.  **Build the Application:**
    ```bash
    mvn clean install
    ```

3.  **Run the Application:**

    *   **Using Maven:**
        ```bash
        export OPENJFS_DATA_PATH=/path/to/your/files
        mvn spring-boot:run
        ```

    *   **From the Executable JAR:**
        ```bash
        export OPENJFS_DATA_PATH=/path/to/your/files
        java -jar target/openjfs-*.jar
        ```

## Usage
Once running, you can access your files at `http://localhost:8080`. 

### Downloading
You can download your files using `http://localhost:8080/direct/path_to_your/file.ext`. Using `OPENJFS_ALLOW_ZIP_DIRECTORIES=true` will zip directories on the fly.

For example, a file located at `${OPENJFS_DATA_PATH}/documents/report.pdf` would be accessible at `http://localhost:8080/direct/documents/report.pdf`.

### JSON Listing
You can get basic file/directory info using `http://localhost:8080/list/path_to_your/dir`.
Example response: 
```json
{
  "path": "",
  "name": "dir",
  "lastModified": "2025-08-04T16:28:37.23",
  "files": [
    {
      "path": "dir/",
      "name": "file1.txt",
      "lastModified": "2025-08-04T15:07:10.41",
      "size": 5,
      "type": "REGULAR_FILE"
    }
  ],
  "type": "DIRECTORY"
}
```

## Configuration

The file server is configured through environment variables.

| Environment Variable            | Description                                                                 | Default Value     |
|---------------------------------|-----------------------------------------------------------------------------|-------------------|
| `OPENJFS_DATA_PATH`             | The absolute path to the directory you want to serve files from.            | Current directory |
| `OPENJFS_PORT`                  | The port for webserver to listen on.                                        | `8080`            |
| `OPENJFS_ALLOW_HIDDEN`          | Set to `true` to allow access to hidden files and directories.              | `true`            |
| `OPENJFS_ALLOW_ZIP_DIRECTORIES` | Set to `true` to enable downloading entire directories as a ZIP archive.    | `true`            |
| `OPENJFS_ZIP_COMPRESSION_LEVEL` | The compression level for ZIP archives, from 0 (no compression) to 9 (max). | `1`               |
| `OPENJFS_REQUEST_TIMEOUT`       | The request timeout (crucial for large file downloads).                     | `3600000`         |

