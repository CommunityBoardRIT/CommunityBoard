package community.auth;

import community.Group;
import community.json.PermRequest;

import java.nio.file.Paths;
import java.util.List;

public class PermissionService {
    private final List<Group> groupCache;

    public PermissionService(List<Group> groupCache) {
        this.groupCache   = groupCache;
    }

    public boolean isAllowed(PermRequest req) {
        List<Group> userGroups = groupCache.stream()
                .filter(g -> g.getUsers().contains(req.username))
                .toList();

        if (userGroups.isEmpty()) {
            // no group, no permissions
            return false;
        }

        Perm op = Perm.valueOf(req.operation);
        String normalizedPath = Paths.get(req.path).getParent() != null
                ? Paths.get(req.path).getParent().toString()
                : ".";

        for (Group g : userGroups) {
            if (g.isAllowed(op, normalizedPath)) {
                return true;
            }
        }

        return false;
    }
}
