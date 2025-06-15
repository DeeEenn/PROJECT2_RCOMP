import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class ClientApp {
    static Scanner scanner = new Scanner(System.in);
    static PrintWriter writer;
    static BufferedReader reader;
    static final String CLIENT_DIR;

//    static {
//        String projectDir = System.getProperty("user.dir");
//        CLIENT_DIR = Paths.get(projectDir, "..", "..", "java_client_files").toAbsolutePath().toString();
//        System.out.println("Project directory: " + projectDir);
//        System.out.println("Client directory: " + CLIENT_DIR);
//    }
// For debugging purposes, you can uncomment the following static block to determine the project root dynamically
    static {

        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path projectRoot = null;

        Path tempPath = currentPath;
        while (tempPath != null && !tempPath.getFileName().toString().equals("PROJECT2_RCOMP")) {
            tempPath = tempPath.getParent();
        }

        if (tempPath != null) {
            projectRoot = tempPath;
        } else {
            System.err.println("Error: Could not determine project root 'PROJECT2_RCOMP'. Using current directory as fallback.");
            projectRoot = currentPath; // Fallback
        }

        CLIENT_DIR = projectRoot.resolve("java_client_files").toAbsolutePath().toString();

        System.out.println("Current working directory (user.dir): " + currentPath);
        System.out.println("Determined Project Root: " + projectRoot);
        System.out.println("Client directory: " + CLIENT_DIR);

        try {
            Files.createDirectories(Paths.get(CLIENT_DIR));
            System.out.println("Using directory: " + CLIENT_DIR);
        } catch (IOException e) {
            System.err.println("Failed to create client directory: " + CLIENT_DIR + " - " + e.getMessage());
            System.exit(1); // Exit if directory creation fails, as it's critical
        }
    }

    public static void main(String[] args) {
        try {
            // Create client directory if it doesn't exist
            Files.createDirectories(Paths.get(CLIENT_DIR));
            System.out.println("Using directory: " + CLIENT_DIR);
            
            Socket socket = new Socket("localhost", 8080);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            authenticate();

            while (true) {
                System.out.println("\n1. LIST\n2. UPLOAD\n3. DOWNLOAD\n4. DELETE\n5. EXIT");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1": listFiles(); break;
                    case "2": uploadFile(); break;
                    case "3": downloadFile(); break;
                    case "4": deleteFile(); break;
                    case "5": socket.close(); return;
                    default: System.out.println("Invalid.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void authenticate() throws IOException {
        System.out.print("ID: ");
        String user = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();
        writer.println("AUTH|" + user + "|" + pass);
        String response = reader.readLine();
        if (!response.equals("AUTH_OK")) {
            System.out.println("Auth failed.");
            System.exit(0);
        }
        System.out.println("Login successful.");
    }

    static void listFiles() throws IOException {
        writer.println("LIST");
        System.out.println(reader.readLine());
    }

    static void uploadFile() throws IOException {
        System.out.print("Slot (0-9): ");
        String slot = scanner.nextLine();
        System.out.print("File name: ");
        String name = scanner.nextLine();
        File file = new File(CLIENT_DIR, name); // Ensure it uses the correct folder
        System.out.println("Trying to open file: " + file.getAbsolutePath());

        if (!file.exists()) {
            System.out.println("File does not exist: " + file.getAbsolutePath());
            return;
        }
        if (file.length() == 0) {
            System.out.println("File is empty: " + file.getAbsolutePath());
            return;
        }

        String content = Files.readString(file.toPath());
        writer.println("UPLOAD|" + slot + "|" + file.getName() + "|" + content);
        System.out.println(reader.readLine());
    }

    static void downloadFile() throws IOException {
        System.out.print("Slot: ");
        String slot = scanner.nextLine();
        writer.println("DOWNLOAD|" + slot);
        String res = reader.readLine();
        if (!res.startsWith("OK|")) {
            System.out.println("Failed: " + res);
            return;
        }
        String[] parts = res.split("\\|", 3);
        Path filePath = Paths.get(CLIENT_DIR, parts[1]); // Save to the correct folder
        System.out.println("Saving file to: " + filePath.toAbsolutePath());
        Files.write(filePath, parts[2].getBytes());
        System.out.println("File saved successfully in " + CLIENT_DIR);
    }

    static void deleteFile() throws IOException {
        System.out.print("Slot: ");
        String slot = scanner.nextLine();
        writer.println("DELETE|" + slot);
        System.out.println(reader.readLine());
    }
}
