package Entities;

/**
 * Enumeration representing various roles within a park management system.
 */
public enum Role {

    /**
     * Role representing a guest.
     */
    ROLE_GUEST(0),

    /**
     * Role representing a single visitor.
     */
    ROLE_SINGLE_VISITOR(1),

    /**
     * Role representing a visitor group guide.
     */
    ROLE_VISITOR_GROUP_GUIDE(2),

    /**
     * Role representing a park employee.
     */
    ROLE_PARK_EMPLOYEE(3),

    /**
     * Role representing a park department manager.
     */
    ROLE_PARK_DEPARTMENT_MGR(4),

    /**
     * Role representing a park manager.
     */
    ROLE_PARK_MGR(5),

    /**
     * Role representing a park support representative.
     */
    ROLE_PARK_SUPPORT_REPRESENTATIVE(6),

    /**
     * Role representing an administrator.
     */
    ROLE_ADMINISTRATOR(7);

    private int role;

    /**
     * Constructs a Role enum with the given integer value.
     *
     * @param role The integer value associated with the role.
     */
    Role(int role) {
        this.role = role;
    }

    /**
     * Converts a string representation of a role to its corresponding Role enum.
     *
     * @param role The string representation of the role.
     * @return The Role enum corresponding to the input string, or null if no matching role is found.
     */
    public static Role stringToRole(String role) {
        switch (role) {
            case "ROLE_GUEST":
                return ROLE_GUEST;
            case "Visitor":
                return ROLE_SINGLE_VISITOR;
            case "ROLE_VISITOR_GROUP_GUIDE":
                return ROLE_VISITOR_GROUP_GUIDE;
            case "Park Employee":
                return ROLE_PARK_EMPLOYEE;
            case "Department Manager":
                return ROLE_PARK_DEPARTMENT_MGR;
            case "Park Manager":
                return ROLE_PARK_MGR;
            case "Support Representative":
                return ROLE_PARK_SUPPORT_REPRESENTATIVE;
            case "ROLE_ADMINISTRATOR":
                return ROLE_ADMINISTRATOR;
            default:
                return null;
        }
    }
}
