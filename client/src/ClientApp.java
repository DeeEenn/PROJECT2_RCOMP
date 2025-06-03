import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Scanner;

public class ClientApp {
    static Scanner scanner = new Scanner(System.in);
    static PrintWriter writer;
    static BufferedReader reader;

    public static void main(String[] args) {
        try {
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
        File file = new File(name);
        if (!file.exists() || file.length() == 0) {
            System.out.println("File missing or empty.");
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
        Files.write(Path.of(parts[1]), parts[2].getBytes());
        System.out.println("Saved as " + parts[1]);
    }

    static void deleteFile() throws IOException {
        System.out.print("Slot: ");
        String slot = scanner.nextLine();
        writer.println("DELETE|" + slot);
        System.out.println(reader.readLine());
    }
}
