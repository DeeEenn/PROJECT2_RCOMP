# Project 2 – Client Application (C Version)

This is the C version of the Client Application for RCOMP Project 2 (Sprint 3).  
It connects to the Java-based File Server using TCP sockets and supports multiple commands such as AUTH, LIST, UPLOAD, DOWNLOAD, and DELETE.

---

##  Requirements

- GCC compiler (Linux/Mac or MinGW/Git Bash for Windows)
- File Server (Java) must be running on `127.0.0.1:8080`

---

##  How to Compile

```bash
cd client/src
gcc Client.c -o client
```

---

##  How to Run

```bash
./client
```

You will be prompted to enter:

- Username (default: `admin`)
- Password (default: `password`)

Then, a menu will allow you to perform the available commands.

---

## Features

| Option | Command | Description |
|--------|---------|-------------|
| 1 | LIST | List all occupied file slots |
| 2 | UPLOAD | Upload a file to a selected slot |
| 3 | DOWNLOAD | Download a file from a selected slot |
| 4 | DELETE | Delete a file from a selected slot |
| 5 | EXIT | Close the client program |

---

##  Notes

- For UPLOAD, make sure the file exists in the same directory as the executable.
- File content is read into memory, so files should be reasonably small (<4KB).
- This client assumes the server uses the same protocol format as defined in `Protocol.java`.

---

## Author

- 1242374 (YouJeong Lee) – C Client Developer, Sprint 3
