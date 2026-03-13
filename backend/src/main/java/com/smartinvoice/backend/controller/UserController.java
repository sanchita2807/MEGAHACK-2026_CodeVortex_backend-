package com.smartinvoice.backend.controller;

import com.smartinvoice.backend.dto.UserDTO;
import com.smartinvoice.backend.model.User;
import com.smartinvoice.backend.repository.UserRepository;
import com.smartinvoice.backend.security.RoleBasedAccessControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all users - Only admins can see all users
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        var currentUserOpt = userRepository.findById(currentUserId);
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }
        
        User currentUser = currentUserOpt.get();
        
        // Only admins can view all users
        if (!RoleBasedAccessControl.canViewAdminData(currentUser)) {
            return ResponseEntity.status(403).body("Access denied");
        }
        
        List<UserDTO> users = userRepository.findAll().stream()
            .map(user -> UserDTO.fromUser(user, true))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get user by ID - Admins can view any user, regular users can only view themselves
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        var currentUserOpt = userRepository.findById(currentUserId);
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }
        
        var targetUserOpt = userRepository.findById(id);
        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        
        User currentUser = currentUserOpt.get();
        User targetUser = targetUserOpt.get();
        
        // Check access permission
        if (!RoleBasedAccessControl.canViewUserData(currentUser, targetUser)) {
            return ResponseEntity.status(403).body("Access denied");
        }
        
        boolean isAdmin = currentUser.getUserType() == RoleBasedAccessControl.ADMIN_TYPE;
        return ResponseEntity.ok(UserDTO.fromUser(targetUser, isAdmin));
    }
    
    /**
     * Update user - Admins can update any user, regular users can only update themselves
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UserDTO updateRequest,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        
        if (currentUserId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        var currentUserOpt = userRepository.findById(currentUserId);
        if (currentUserOpt.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }
        
        var targetUserOpt = userRepository.findById(id);
        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        
        User currentUser = currentUserOpt.get();
        User targetUser = targetUserOpt.get();
        
        // Check modification permission
        if (!RoleBasedAccessControl.canModifyUserData(currentUser, targetUser)) {
            return ResponseEntity.status(403).body("Access denied");
        }
        
        // Regular users cannot modify userType or passwordSet
        if (currentUser.getUserType() == RoleBasedAccessControl.USER_TYPE) {
            updateRequest.setUserType(null);
            updateRequest.setPasswordSet(null);
        }
        
        // Update allowed fields
        if (updateRequest.getName() != null) targetUser.setName(updateRequest.getName());
        if (updateRequest.getPhone() != null) targetUser.setPhone(updateRequest.getPhone());
        if (updateRequest.getShopName() != null) targetUser.setShopName(updateRequest.getShopName());
        if (updateRequest.getBusinessType() != null) targetUser.setBusinessType(updateRequest.getBusinessType());
        
        // Only admins can update userType and passwordSet
        if (currentUser.getUserType() == RoleBasedAccessControl.ADMIN_TYPE) {
            if (updateRequest.getUserType() != null) targetUser.setUserType(updateRequest.getUserType());
            if (updateRequest.getPasswordSet() != null) targetUser.setPasswordSet(updateRequest.getPasswordSet());
        }
        
        userRepository.save(targetUser);
        
        boolean isAdmin = currentUser.getUserType() == RoleBasedAccessControl.ADMIN_TYPE;
        return ResponseEntity.ok(UserDTO.fromUser(targetUser, isAdmin));
    }
}
