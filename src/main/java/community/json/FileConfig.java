package community.json;

public class FileConfig {
    public String rootDirectory;
    public String userPropertiesPath;

    public FileConfig(String rootDirectory, String userPropertiesPath) {
        this.rootDirectory = rootDirectory;
        this.userPropertiesPath = userPropertiesPath;
    }

    public FileConfig() {}
}
