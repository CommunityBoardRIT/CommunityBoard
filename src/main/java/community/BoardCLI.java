package community;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import com.google.gson.Gson;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoardCLI {
    FTPClient ftpClient;

    String username;
    String password;

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
        ftpClient.enterLocalPassiveMode();
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
    public static String[] tokenizeCommand(String line) {
        List<String> tokens = new ArrayList<>();
        Pattern pattern = Pattern.compile("([^\"\\s]+)|\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // unquoted
                tokens.add(matcher.group(1));
            } else {
                // strip quotes
                tokens.add(matcher.group(2));
            }
        }
        return tokens.toArray(new String[0]);
    }

    public boolean permission_request(Perm operation){
        var values = new HashMap<String, String>() {{
            put("username", username);
            put("password", password);
            put("operation", String.valueOf(operation));
        }};

        Gson gson = new Gson();
        String requestBody = gson.toJson(values);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("url"))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());
            if (response.statusCode() == 200){
                return true;
            }
            else {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void commandLoop() {
        Scanner scanner = new Scanner(System.in);

        try {
            boolean loggedIn = false;

            // Login loop
            while (!loggedIn) {
                System.out.println("Please enter your credentials.");
                System.out.print("Username: ");
                username = scanner.nextLine().trim();
                System.out.print("Password: ");
                password = scanner.nextLine().trim();

                System.out.println("username: " + username + " password: " + password);
                loggedIn = login(username, password);

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
                String[] tokens = tokenizeCommand(input);
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
                            System.out.println("Example: " + "u \"C:\\Users\\Tristen\\Downloads\\pthreads\" \"pthreads\"");
                        }

                        else{
                            String localFilePath = tokens[1];
                            String fileName = tokens[2];

                            try (FileInputStream inputStream = new FileInputStream(localFilePath)) {
                                boolean uploaded = ftpClient.storeFile(fileName, inputStream);
                                System.out.println("REPLY: " + ftpClient.getReplyString());
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
                            System.out.println("Example: " + "\"pthreads\" \"C:\\Users\\Tristen\\Downloads\\pthreads\"");
                        }
                        else{
                            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tokens[2]));
                            boolean success = ftpClient.retrieveFile(tokens[1], outputStream);
                            outputStream.close();


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