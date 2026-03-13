package com.smartinvoice.backend.security;

import com.smartinvoice.backend.model.User;

public class RoleBasedAccessControl {
    
    public static final int USER_TYPE = 0;
    public static final int ADMIN_TYPE = 1;
    
    /**
     * Check if user can view admin data
     * Only admins (userType = 1) can view admin data
     */
    public static boolean canViewAdminData(User currentUser) {
        return currentUser != null && currentUser.getUserType() == ADMIN_TYPE;
    }
    
    /**
     * Check if user can modify admin data
     * Only admins (userType = 1) can modify admin data
     */
    public static boolean canModifyAdminData(User currentUser) {
        return currentUser != null && currentUser.getUserType() == ADMIN_TYPE;
    }
    
    /**
     * Check if user can view user data
     * Admins can view all user data, regular users can only view their own
     */
    public static boolean canViewUserData(User currentUser, User targetUser) {
        if (currentUser == null) return false;
        if (currentUser.getUserType() == ADMIN_TYPE) return true;
        return currentUser.getId().equals(targetUser.getId());
    }
    
    /**
     * Check if user can modify user data
     * Admins can modify all user data, regular users can only modify their own
     */
    public static boolean canModifyUserData(User currentUser, User targetUser) {
        if (currentUser == null) return false;
        if (currentUser.getUserType() == ADMIN_TYPE) return true;
        return currentUser.getId().equals(targetUser.getId());
    }
    
    /**
     * Filter user data based on role
     * Admins see all fields, regular users don't see admin-related fields
     */
    public static User filterUserData(User user, User currentUser) {
        if (currentUser == null) return null;
        
        // If current user is admin, return full data
        if (currentUser.getUserType() == ADMIN_TYPE) {
            return user;
        }
        
        // Regular users cannot see userType and passwordSet fields
        if (currentUser.getId().equals(user.getId())) {
            return user;
        }
        
        return null;
    }
}
