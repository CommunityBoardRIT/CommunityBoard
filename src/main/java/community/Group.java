package community;

import java.util.List;
import java.util.Map;

public class Group {
    String name;


    /** directory, permissions **/
    public Map<String, List<Perm>> permissions;

    /** users inside the group **/
    public List<String> users;
    public Group(String name){
        this.name = name;
    }

    private void renameGroup(String newName){
        this.name = newName;
    }

    private void addPermissions(String directory, List<Perm> perms){
        permissions.put(directory, perms);
    }

    private void removePermission(String directory, Perm perm){
        permissions.remove(directory, perm);
    }

    private void removePermissions(String directory, List<Perm> perms){
        for (int i = 0; i < perms.size(); i++){
            permissions.remove(directory, perms.get(i));
        }
    }

    private void addUser(String username){
        users.add(username);
    }

    private void addUsers(List<String> usernames){
        users.addAll(usernames);
    }

    private void removeUser(String username){
        users.remove(username);
    }


}
