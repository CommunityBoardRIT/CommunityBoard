package community;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;

public class Board {

    FtpServer server;
    ListenerFactory listenerFactory;
    UserManager userManager;
    public Board() throws FtpException {
        FtpServerFactory serverFactory = new FtpServerFactory();
        listenerFactory = new ListenerFactory();
        serverFactory.addListener("default", listenerFactory.createListener());

        // We should use JSON to hold configurations like FTP root file and user.properties path
        // The executable should require an argument for the path of the config file
        // Default config path should be in the root of the project
        // Should include checking if the users.properties file exists; currently throws error
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(new java.io.File("users.properties"));
        userManager = userManagerFactory.createUserManager();
        serverFactory.setUserManager(userManager);

        server = serverFactory.createServer();

        createAdmin("admin", "password");
    }

    public void createAdmin(String user, String password) throws FtpException {
        BaseUser adminUser = new BaseUser();
        adminUser.setName(user);
        adminUser.setPassword(password);
        userManager.save(adminUser);
        System.out.println("Admin account created successfully: " + user + ":" + password);
    }
    public void startServer() throws FtpException {
        server.start();

        System.out.println("FTP Server started on port " + listenerFactory.getPort());
    }

    public static void main(String[] args) throws FtpException {
        Board board = new Board();
        board.startServer();
    }
}
