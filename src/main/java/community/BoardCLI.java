package community;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
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
                System.out.print("board" + ftpClient.printWorkingDirectory() + ">");
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
                    case "cd" -> {
                        if (tokens.length < 2){
                            System.out.println("Command usage: cd {directory}");
                        }
                        ftpClient.changeWorkingDirectory(tokens[1]);
                    }
                    case "d" -> {
                        if (tokens.length < 2){
                            System.out.println("Command usage: d {file}");
                        }
                        ftpClient.deleteFile(tokens[1]);
                    }

                    // we are selling our product to people who do not accidentally make directories - brooke 2025
                    case "m" -> {
                        if (tokens.length < 2){
                            System.out.println("Command usage: m {name}");
                        }
                        ftpClient.makeDirectory(tokens[1]);
                    }

                    // just kidding
                    // w for wombo
                    case "w" -> {
                        if (tokens.length < 2){
                            System.out.println("Command usage: w {name}");
                        }
                        ftpClient.removeDirectory(tokens[1]);
                    }
                    case "u" -> {
                        if (tokens.length < 3){
                            System.out.println("Command usage: u {localFilePath} {remoteFilePath}");
                        }

                        else{
                            String localFilePath = tokens[1];
                            String fileName = tokens[2];

                            System.out.println(localFilePath);
                            System.out.println(fileName);

                            try (FileInputStream inputStream = new FileInputStream(localFilePath)) {
                                boolean uploaded = ftpClient.storeFile(fileName, inputStream);
                                if (uploaded) {
                                    System.out.println("File uploaded successfully.");
                                } else {
                                    System.err.println("File upload failed.");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                    }

                    case "r" -> {
                        if (tokens.length < 3){
                            System.out.println("Command usage: r {remoteFilePath} {localFilePath}");
                        }
                        else{
                            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tokens[2]));
                            boolean success = ftpClient.retrieveFile(tokens[1], outputStream);
                            outputStream.close();

                            System.out.println(tokens[1]);
                            System.out.println(tokens[2]);

                            if (success) {
                                System.out.println("File has been downloaded successfully.");
                            }
                            else{
                                System.out.println("Download failure.");
                            }
                        }

                    }

                    default -> {
                        System.out.println("Unknown command: " + command);
                        System.out.println("Available commands:");
                        System.out.println("l: list all files in current directory");
                        System.out.println("q: logout and disconnect from the board");
                        System.out.println("cd: navigate to a different directory");
                        System.out.println("u {localFilePath} {remoteFilePath}: upload a local file to the board");
                        System.out.println("r {remoteFilePath} {localFilePath}: upload a board file to local");
                        System.out.println("m {file directory}: create a file directory");
                        System.out.println("w {file directory}: remove a file directory");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}