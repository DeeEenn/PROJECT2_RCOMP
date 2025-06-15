# RCOMP - Project 2 - Java Client Application

This is the Java version of the Client Application for RCOMP Project 2, designed to interact with the Java-based File Server. It utilizes TCP sockets for communication and supports a range of file management commands.

---

## Requirements

* Java 11 or higher
* File Server (Java) must be running on `127.0.0.1:8080`

---

## How to Compile

Assuming you have a `pom.xml` configured for the client or are compiling directly:

**Bash**

```
cd client/src/main/java/com/fileserver/
javac ClientApp.java
```

*(Note: adjust the `cd` command accordingly if it's placed in `com.fileserver` as per the server's project structure.)*

---

## How to Run

**Bash**

```
java ClientApp
```

You will be prompted to enter:

* Username (default: `admin`)
* Password (default: `admin`)

Then, a menu will allow you to perform the available commands.

---

## Features

The client supports the following operations, corresponding to the server's protocol:


| **Option** | **Command** | **Description**                      |
| ---------- | ----------- | ------------------------------------ |
| 1          | `LIST`      | List all occupied file slots         |
| 2          | `UPLOAD`    | Upload a file to a selected slot     |
| 3          | `DOWNLOAD`  | Download a file from a selected slot |
| 4          | `DELETE`    | Delete a file from a selected slot   |
| 5          | `EXIT`      | Close the client program             |

The client also handles user authentication via the `AUTH` command.

---

## Configuration

The client application's current configuration assumes:

* **Server IP:**`localhost`
* **Server Port:**`8080`
* **Client Files Directory:** Files for upload and download are handled in a directory named `java_client_files` relative to the project structure (specifically `../../java_client_files` from the `ClientApp.java`'s execution directory).

---

## Error Handling

The client includes basic error handling for:

* File existence during upload.
* Empty files during upload.
* Failed download responses from the server.
* Authentication failures.
* General exceptions during socket operations.

---

## Notes

* For `UPLOAD`, ensure the file exists in the `java_client_files` directory relative to where the client is run.
* File content for `UPLOAD` and `DOWNLOAD` is read and sent/received as a whole string. This means files should be reasonably small to avoid memory issues.
* This client assumes the server uses the same protocol format as defined.
* The system uses SSL/TLS for encrypted communication.

---

## Author

* **1232162** - YASAMIN EBRAHIMI
