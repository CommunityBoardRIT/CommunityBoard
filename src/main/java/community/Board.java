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
