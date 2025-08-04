# Open Java File Server (🛠️ WIP 🛠️)

A simple, open-source Java Spring Boot application designed to serve static files from a specified directory. It provides straightforward, unauthenticated access to your files, for easy personal or internal network file sharing.

**Please note: This project is under active development. Expect changes and potential instability.**

## Features

*   **Directory Download (as ZIP):** Download entire directories as a single ZIP archive.
*   **Direct File Download:** Access and download individual files directly through a simple URL structure.

## Roadmap

*   [ ] **JSON Directory Listings** 
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

    Once running, you can access your files at `http://localhost:8080`. For example, a file located at `/path/to/your/files/documents/report.pdf` would be accessible at `http://localhost:8080/direct/documents/report.pdf`.

## Configuration

The application is configured through environment variables.

| Environment Variable            | Description                                                                  | Default Value     |
|---------------------------------|------------------------------------------------------------------------------|-------------------|
| `OPENJFS_DATA_PATH`             | The absolute path to the directory you want to serve files from.             | Current directory |
| `OPENJFS_PORT`                  | The port for webserver.                                                      | `8080`            |
| `OPENJFS_ALLOW_HIDDEN`          | Set to `true` to allow access to hidden files and directories.               | `true`            |
| `OPENJFS_ALLOW_ZIP_DIRECTORIES` | Set to `true` to enable downloading entire directories as a ZIP archive.     | `true`            |
| `OPENJFS_ZIP_COMPRESSION_LEVEL` | The compression level for ZIP archives, from -1 (no compression) to 9 (max). | `3`               |
| `OPENJFS_REQUEST_TIMEOUT`       | The request timeout (crucial for large file downloads)                       | `3600000`         |

