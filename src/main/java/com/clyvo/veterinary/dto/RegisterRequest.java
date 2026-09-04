package com.clyvo.veterinary.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "O nome nao pode ser vazio")
    private String name;

    @NotBlank(message = "O email e obrigatorio")
    @Email(message = "Formato de email invalido")
    private String email;

    @NotBlank(message = "A senha e obrigatoria")
    @Size(min = 3, message = "A senha deve ter no minimo 3 digitos")
    private String password;

    @NotNull(message = "O perfil (role) e obrigatorio")
    private String role; // TUTOR, VETERINARIO, CLINICA

    private Integer age;
    private String phone;
    private String crmv;
    private String cnpj;
    private String document; // CPF/CNPJ
    
    // Getters and Setters
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; } public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; } public void setRole(String role) { this.role = role; }
    public Integer getAge() { return age; } public void setAge(Integer age) { this.age = age; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public String getCrmv() { return crmv; } public void setCrmv(String crmv) { this.crmv = crmv; }
    public String getCnpj() { return cnpj; } public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getDocument() { return document; } public void setDocument(String document) { this.document = document; }
}
