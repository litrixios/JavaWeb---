package com.bjfu.cms.entity.dto;

public class RegisterDTO {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String role;
    private String affiliation;
    private String researchDirection;

    // getter 和 setter 方法
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAffiliation() { return affiliation; }
    public void setAffiliation(String affiliation) { this.affiliation = affiliation; }

    public String getResearchDirection() { return researchDirection; }
    public void setResearchDirection(String researchDirection) { this.researchDirection = researchDirection; }
}