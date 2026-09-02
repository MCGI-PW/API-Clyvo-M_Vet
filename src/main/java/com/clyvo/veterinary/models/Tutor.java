package com.clyvo.veterinary.models;
import jakarta.persistence.*;
import java.util.UUID;
@Entity
@Table(name = "tutors")
public class Tutor {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private Integer age;
    @Column(nullable = false) private String phone;
    @OneToOne @JoinColumn(name = "user_id", referencedColumnName = "id") private User user;
    
    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; } public void setAge(Integer age) { this.age = age; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public User getUser() { return user; } public void setUser(User user) { this.user = user; }
}
