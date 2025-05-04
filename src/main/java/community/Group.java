package community;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Group {

    public String name;

    /** directory, permissions **/
    private Map<String, List<Perm>> permissions;

    /** users inside the group **/
    private List<String> users;
    public Group(String name){

        this.name = name;
        this.permissions = new HashMap<>();
        this.users = new ArrayList<>();
    }

    public HashMap<String, List<Perm>> getPermissions() {
        return (HashMap<String, List<Perm>>) permissions;
    }

    public List<String> getUsers() {
        return users;
    }

    public void renameGroup(String newName){
        this.name = newName;
    }

    public void addPermissions(String directory, List<Perm> perms){
        permissions.put(directory, perms);
    }

    public void removePermission(String directory, Perm perm){
        permissions.remove(directory, perm);
    }

    public void removePermissions(String directory, List<Perm> perms){
        for (int i = 0; i < perms.size(); i++){
            permissions.remove(directory, perms.get(i));
        }
    }

    public void addUser(String username){
        users.add(username);
    }

    public void addUsers(List<String> usernames){
        users.addAll(usernames);
    }

    public void removeUser(String username){
        users.remove(username);
    }

    public void save(){
        ObjectMapper mapper = new ObjectMapper();
        // save arguments in json file
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("groupConfig.json"), this);
        } catch (IOException e) {
            System.out.println("Error writing to JSON: " + e);
            throw new RuntimeException(e);
        }
    }

}