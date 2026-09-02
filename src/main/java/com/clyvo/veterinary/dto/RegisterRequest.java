package com.clyvo.veterinary.dto;
import com.clyvo.veterinary.models.Role;
public class RegisterRequest {
    private String name; private String email; private String password; private Role role;
    private Integer age; private String phone; private String crmv; // Optional fields based on role
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; } public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; } public void setRole(Role role) { this.role = role; }
    public Integer getAge() { return age; } public void setAge(Integer age) { this.age = age; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public String getCrmv() { return crmv; } public void setCrmv(String crmv) { this.crmv = crmv; }
}
