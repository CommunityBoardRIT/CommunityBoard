package community;

import com.fasterxml.jackson.databind.ObjectMapper;
import community.json.FileConfig;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;

public class Board {

    FtpServer server;
    ListenerFactory listenerFactory;
    UserManager userManager;

    String rootDirectory;

    String userPropertiesPath;
    public Board() throws FtpException {
        FtpServerFactory serverFactory = new FtpServerFactory();
        listenerFactory = new ListenerFactory();
        serverFactory.addListener("default", listenerFactory.createListener());

        // We should use JSON to hold configurations like FTP root file and user.properties path
        // The executable should require an argument for the path of the config file
        // Default config path should be in the root of the project
        // Should include checking if the users.properties file exists; currently throws error

        // check for config file
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get("config.json")));
        } catch (IOException e) {
            System.out.println("Error: " + e);
            throw new RuntimeException(e);
        }
        JSONObject config = new JSONObject(content);

        rootDirectory = config.getString("rootDirectory");
        userPropertiesPath = config.getString("userPropertiesPath");

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(new java.io.File(userPropertiesPath + "//user.properties"));
        userManager = userManagerFactory.createUserManager();
        serverFactory.setUserManager(userManager);

        server = serverFactory.createServer();

        createAdmin("admin", "password");
    }

    public void createUser(String user, String password) throws FtpException {
        BaseUser newUser = new BaseUser();
        newUser.setName(user);
        newUser.setPassword(password);
        newUser.setHomeDirectory(rootDirectory + '/' + user);
        userManager.save(newUser);
        System.out.println("User account created successfully: " + user + ":" + password);
    }

    public void createAdmin(String user, String password) throws FtpException {
        BaseUser adminUser = new BaseUser();
        adminUser.setName(user);
        adminUser.setPassword(password);

        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        adminUser.setAuthorities(authorities);
        adminUser.setHomeDirectory(rootDirectory);
        userManager.save(adminUser);
        System.out.println("Admin account created successfully: " + user + ":" + password);
    }
    public void startServer() throws FtpException {
        server.start();

        System.out.println("FTP Server started on port " + listenerFactory.getPort());
    }

    public static void main(String[] args) throws FtpException {
        if (args.length != 2) {
            System.out.println("Usage: java -jar Main.jar <rootDirectory> <userpropertiesPath>");
            System.exit(1);
        }

        // process command arguments
        String rootDir = args[0];
        String userPropsPath = args[1];

        FileConfig config = new FileConfig(rootDir, userPropsPath);

        ObjectMapper mapper = new ObjectMapper();
        // save arguments in json file
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("config.json"), config);
        } catch (IOException e) {
            System.out.println("Error writing to JSON: " + e);
            throw new RuntimeException(e);
        }

        System.out.println("File config saved to config.json");

        // Creating user.properties file if it doesn't already exist
        File propertiesFile = new File(userPropsPath + "\\user.properties");
        if (!propertiesFile.exists()){
            try {
                boolean created = propertiesFile.createNewFile();
                if (created) {
                    System.out.println("Empty user.properties file created at: " + propertiesFile.getAbsolutePath());
                } else {
                    System.out.println("Failed to create file.");
                }
            } catch (IOException e) {
                System.out.println("Error creating user.properties file: " + e);
                throw new RuntimeException(e);
            }
        }
        else {
            System.out.println("user.properties already exists at this location... continuing");
        }

        Board board = new Board();
        board.startServer();
    }
}
