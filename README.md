# RCOMP - Project 2 - File Sharing System 
## Team members
- **(1242374)** - YOUJEONG LEE
- **(1232162)** - YASAMIN EBRAHIMI
- **(1242300)** - DAVID NIC

## Table of Contents
- [Project Overview](#project-overview)
- [Project Structure](#project-structure)
- [Server Implementation](#server-implementation)
- [Client Implementation](#client-implementation)
- [Protocol Specification](#protocol-specification)
- [Configuration](#configuration)
- [Usage](#usage)

## Project Overview

A file sharing system with server and client implementations in Java and C. The system supports basic file operations and secure communication.

## Project Structure

```
PROJECT2_RCOMP/
├── server/                 # Java server implementation
│   └── src/
│       └── main/
├── client/                 # Client implementations
│   └── src/
│       ├── ClientApp.java  # Java client
│       └── Client.c        # C client
├── java_client_files/      # Directory for Java client files
├── c_client_files/         # Directory for C client files
├── server_storage/         # Server storage directory
└── test_server.py         # Server testing script
```

## Server Implementation

The server is implemented in Java and provides the following features:
- File slot management (max 10 slots)
- User authentication
- File operations (upload, download, delete)
- Slot content listing

## Client Implementation

### Java Client (ClientApp.java)
- Implements all required operations
- Uses TCP socket for communication
- Manages files in java_client_files directory

### C Client (Client.c)
- Implements all required operations
- Uses TCP socket for communication
- Manages files in c_client_files directory

## Protocol Specification

### Request Commands
| Command | Format | Description |
|---------|--------|-------------|
| `AUTH` | `AUTH|username|password` | User authentication |
| `LIST` | `LIST` | List occupied slots |
| `UPLOAD` | `UPLOAD|slot|filename|content` | Upload file |
| `DOWNLOAD` | `DOWNLOAD|slot` | Download file |
| `DELETE` | `DELETE|slot` | Delete file |

### Response Commands
| Command | Description |
|---------|-------------|
| `AUTH_OK` | Successful authentication |
| `AUTH_FAIL` | Failed authentication |
| `OK|filename|content` | Successful file operation |
| `ERROR` | Operation error |

## Configuration

### Server Configuration
- Port: 8080 (default)
- Maximum number of slots: 10
- Username and password are configurable at startup

### Client Configuration
- Server address: localhost
- Port: 8080
- File directories are automatically created

## Usage

### Starting the Server
```bash
cd server
javac src/main/java/*.java
java -cp src/main/java Server [port] [username] [password]
```

### Starting Java Client
```bash
cd client
javac src/ClientApp.java
java -cp src ClientApp
```

### Starting C Client
```bash
cd client
gcc src/Client.c -o client
./client
```

### Client Operations
1. Login using username and password
2. Select operation from menu:
   - LIST - list files
   - UPLOAD - upload file
   - DOWNLOAD - download file
   - DELETE - delete file
   - EXIT - exit
