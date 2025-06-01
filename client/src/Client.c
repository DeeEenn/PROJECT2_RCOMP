#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>

#define SERVER_IP "127.0.0.1"
#define SERVER_PORT 8080
#define BUFFER_SIZE 4096

void uploadFile(int sockfd);
void downloadFile(int sockfd);
void deleteFile(int sockfd);

int main() {
    int sockfd;
    struct sockaddr_in server_addr;
    char send_buf[BUFFER_SIZE];
    char recv_buf[BUFFER_SIZE];
    int len;

    // Create socket
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
        perror("socket error");
        exit(EXIT_FAILURE);
    }

    // Configure server address
    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(SERVER_PORT);
    if (inet_pton(AF_INET, SERVER_IP, &server_addr.sin_addr) <= 0) {
        perror("inet_pton error");
        close(sockfd);
        exit(EXIT_FAILURE);
    }

    // Connect to server
    if (connect(sockfd, (struct sockaddr*)&server_addr, sizeof(server_addr)) < 0) {
        perror("connect error");
        close(sockfd);
        exit(EXIT_FAILURE);
    }

    printf("Connected to Server\n");

    // Ask for credentials
    char username[64], password[64];
    printf("ID: ");
    fgets(username, sizeof(username), stdin);
    username[strcspn(username, "\n")] = 0;

    printf("Password: ");
    fgets(password, sizeof(password), stdin);
    password[strcspn(password, "\n")] = 0;

    // Send AUTH
    snprintf(send_buf, sizeof(send_buf), "AUTH|%s|%s\n", username, password);
    send(sockfd, send_buf, strlen(send_buf), 0);

    len = recv(sockfd, recv_buf, sizeof(recv_buf) - 1, 0);
    if (len > 0) {
        recv_buf[len] = '\0';
        if (strncmp(recv_buf, "AUTH_FAIL", 9) == 0) {
            printf("Auth Fail, Close the program\n");
            close(sockfd);
            return 1;
        }
        printf("Login successful\n");
    }

    // Main menu loop
    while (1) {
        char choice[16];
        printf("\nSelect an option:\n");
        printf("1. LIST\n");
        printf("2. UPLOAD\n");
        printf("3. DOWNLOAD\n");
        printf("4. DELETE\n");
        printf("5. EXIT\n");
        printf("Enter choice: ");

        fgets(choice, sizeof(choice), stdin);
        choice[strcspn(choice, "\n")] = 0;

        if (strcmp(choice, "1") == 0) {
            snprintf(send_buf, sizeof(send_buf), "LIST\n");
            send(sockfd, send_buf, strlen(send_buf), 0);
            len = recv(sockfd, recv_buf, sizeof(recv_buf) - 1, 0);
            if (len > 0) {
                recv_buf[len] = '\0';
                printf("Response: %s\n", recv_buf);
            }
        } else if (strcmp(choice, "2") == 0) {
            uploadFile(sockfd);
        } else if (strcmp(choice, "3") == 0) {
            downloadFile(sockfd);
        } else if (strcmp(choice, "4") == 0) {
            deleteFile(sockfd);
        } else if (strcmp(choice, "5") == 0) {
            printf("Exiting...\n");
            break;
        } else {
            printf("Invalid choice. Please try again.\n");
        }
    }

    close(sockfd);
    return 0;
}

// Upload a file to a slot
void uploadFile(int sockfd) {
    char slot_input[16];
    char filename[256];
    char filepath[512];
    char content[BUFFER_SIZE];
    char send_buf[BUFFER_SIZE];
    char recv_buf[BUFFER_SIZE];
    int len;

    printf("Enter slot number to upload into: ");
    fgets(slot_input, sizeof(slot_input), stdin);
    slot_input[strcspn(slot_input, "\n")] = 0;

    printf("Enter file name to upload: ");
    fgets(filename, sizeof(filename), stdin);
    filename[strcspn(filename, "\n")] = 0;

    snprintf(filepath, sizeof(filepath), "%s", filename);

    FILE *fp = fopen(filepath, "r");
    if (fp == NULL) {
        perror("Failed to open file");
        return;
    }

    fread(content, 1, sizeof(content) - 1, fp);
    content[sizeof(content) - 1] = '\0';
    fclose(fp);

    snprintf(send_buf, sizeof(send_buf), "UPLOAD|%s|%s|%s\n", slot_input, filename, content);
    send(sockfd, send_buf, strlen(send_buf), 0);
    printf("Sent: UPLOAD|%s|%s|<file_content>\n", slot_input, filename);

    len = recv(sockfd, recv_buf, sizeof(recv_buf) - 1, 0);
    if (len > 0) {
        recv_buf[len] = '\0';
        printf("Response: %s\n", recv_buf);
    }
}

// Download a file from a slot
void downloadFile(int sockfd) {
    char slot_input[16];
    char send_buf[BUFFER_SIZE];
    char recv_buf[BUFFER_SIZE];
    int len;

    printf("Enter slot number to download from: ");
    fgets(slot_input, sizeof(slot_input), stdin);
    slot_input[strcspn(slot_input, "\n")] = 0;

    snprintf(send_buf, sizeof(send_buf), "DOWNLOAD|%s\n", slot_input);
    send(sockfd, send_buf, strlen(send_buf), 0);
    printf("Sent: %s", send_buf);

    len = recv(sockfd, recv_buf, sizeof(recv_buf) - 1, 0);
    if (len <= 0) {
        printf("Failed to receive response from server.\n");
        return;
    }

    recv_buf[len] = '\0';
    printf("Response: %s\n", recv_buf);

    if (strncmp(recv_buf, "OK|", 3) == 0) {
        char *filename = strtok(recv_buf + 3, "|");
        char *filecontent = strtok(NULL, "");

        if (filename == NULL || filecontent == NULL) {
            printf("Malformed response.\n");
            return;
        }

        FILE *fp = fopen(filename, "w");
        if (fp == NULL) {
            perror("Failed to create file");
            return;
        }

        fwrite(filecontent, 1, strlen(filecontent), fp);
        fclose(fp);
        printf("File '%s' saved successfully.\n", filename);
    } else {
        printf("Download failed: %s\n", recv_buf);
    }
}

// Delete a file from a slot
void deleteFile(int sockfd) {
    char slot_input[16];
    char send_buf[BUFFER_SIZE];
    char recv_buf[BUFFER_SIZE];
    int len;

    printf("Enter slot number to delete from: ");
    fgets(slot_input, sizeof(slot_input), stdin);
    slot_input[strcspn(slot_input, "\n")] = 0;

    snprintf(send_buf, sizeof(send_buf), "DELETE|%s\n", slot_input);
    send(sockfd, send_buf, strlen(send_buf), 0);
    printf("Sent: %s", send_buf);

    len = recv(sockfd, recv_buf, sizeof(recv_buf) - 1, 0);
    if (len > 0) {
        recv_buf[len] = '\0';
        printf("Response: %s\n", recv_buf);
    }
}
