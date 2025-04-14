package community;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.Scanner;

public class BoardCLI {
    FTPClient ftpClient;

    public BoardCLI() {
        ftpClient = new FTPClient();
    }

    public boolean login(String user, String pass) throws IOException {
        String server = "localhost";
        int port = 21;

        ftpClient.connect(server, port);
        if (!ftpClient.login(user, pass)) {
            System.out.println("FTP login failed. Try again...");
            return false;
        }
        return true;
    }

    public void commandLoop() {
        Scanner scanner = new Scanner(System.in);

        try {
            boolean loggedIn = false;

            // Login loop
            while (!loggedIn) {
                System.out.println("Please enter your credentials.");
                System.out.print("Username: ");
                String user = scanner.nextLine().trim();
                System.out.print("Password: ");
                String password = scanner.nextLine().trim();

                System.out.println("username: " + user + " password: " + password);
                loggedIn = login(user, password);

                if (!loggedIn) {
                    System.out.println("Login failed. Please try again.");
                }
            }
            System.out.println("Connected to FTP server.");

            // Command loop
            while (true) {
                System.out.print("board> ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }
                String[] tokens = input.split(" ");
                String command = tokens[0].toLowerCase();

                switch (command) {
                    case "l" -> {
                        FTPFile[] files = ftpClient.listFiles();
                        for (FTPFile file : files) {
                            System.out.println(file.getName());
                        }
                    }
                    case "q" -> {
                        ftpClient.logout();
                        ftpClient.disconnect();
                        System.out.println("Disconnected!");
                        return;
                    }
                    default -> {
                        System.out.println("Unknown command: " + command);
                        System.out.println("Available commands:");
                        System.out.println("l: list all files in current directory");
                        System.out.println("q: logout and disconnect from the board");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}