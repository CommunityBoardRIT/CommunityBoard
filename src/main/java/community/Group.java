package community;

import community.auth.Perm;

import java.nio.file.Path;
import java.nio.file.Paths;
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

    // no-arg constructor for Jackson
    public Group() {
        this.name = "";
        this.permissions = new HashMap<>();
        this.users = new ArrayList<>();
    }
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

    public void removePermission(String dir, Perm perm) {
        List<Perm> list = permissions.get(dir);
        if (list != null) {
            list.remove(perm);
            if (list.isEmpty()) permissions.remove(dir);
        }
    }

    public void removePermissions(String dir, List<Perm> permsToRemove) {
        List<Perm> list = permissions.get(dir);
        if (list != null) {
            list.removeAll(permsToRemove);
            if (list.isEmpty()) permissions.remove(dir);
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

    public boolean isAllowed(Perm op, String resourcePath) {
        // normalize
        Path resource = Paths.get(resourcePath).normalize();

        // find the longest matching directory key
        String bestMatch = null;
        for (String dirKey : permissions.keySet()) {
            Path dir = Paths.get(dirKey).normalize();
            if (resource.startsWith(dir)) {
                if (bestMatch == null ||
                        dir.toString().length() > Paths.get(bestMatch).toString().length())
                {
                    bestMatch = dir.toString();
                }
            }
        }

        if (bestMatch == null) {
            // no directory rule applies
            return false;
        }

        List<Perm> allowed = permissions.get(bestMatch);
        return allowed != null && allowed.contains(op);
    }

}