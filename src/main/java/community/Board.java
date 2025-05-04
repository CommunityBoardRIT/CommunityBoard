package community;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import community.auth.Perm;
import community.auth.PermissionHandler;
import community.auth.PermissionService;
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
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class Board {

    FtpServer server;
    ListenerFactory listenerFactory;
    UserManager userManager;

    List<Group> groupCache;

    String rootDirectory;

    public ObjectMapper objectMapper;
    String userPropertiesPath;
    PermissionService permissionService;
    public Board() throws FtpException {
        groupCache = new ArrayList<>();
        objectMapper = new ObjectMapper();
        FtpServerFactory serverFactory = new FtpServerFactory();
        listenerFactory = new ListenerFactory();
        serverFactory.addListener("default", listenerFactory.createListener());

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
        ArrayList<Perm> permissions = new ArrayList<>();
        permissions.add(Perm.READ);
        permissions.add(Perm.WRITE);
        Group newGroup = createGroup("swagger", "", permissions);
        newGroup.addUser("admin");
        saveGroupCache();

        loadPermissions();
        permissionService = new PermissionService(groupCache);

        try {
            startPermissionEndpoint(9000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void startPermissionEndpoint(int port) throws IOException {
        HttpServer http = HttpServer.create(new InetSocketAddress(port), 0);
        http.createContext("/check", new PermissionHandler(permissionService));
        http.setExecutor(Executors.newCachedThreadPool());
        http.start();
        System.out.println("Permission service listening on http://localhost:" + port + "/check");
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

    public Group createGroup(String name, String defaultDirectory, List<Perm> perms){
        Group group = new Group(name);
        group.addPermissions(defaultDirectory, perms);
        groupCache.add(group);

        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File("groupConfig.json"), groupCache);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return group;
    }

    public void saveGroupCache(){
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File("groupConfig.json"), groupCache);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadPermissions(){
        List <Group> objects;
        try {
            objects = objectMapper.readValue(new File("groupConfig.json"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Group.class));
            objects.forEach(System.out::println);
            groupCache = objects;
        } catch (StreamReadException | DatabindException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
