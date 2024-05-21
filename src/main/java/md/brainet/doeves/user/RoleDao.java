package md.brainet.doeves.user;

public interface RoleDao {
    void insertRoleForUserId(Integer userId, Role role);
}
