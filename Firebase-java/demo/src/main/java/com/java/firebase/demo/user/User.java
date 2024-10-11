package com.java.firebase.demo.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class User {
    private String userName;
    private String name;
    private String birthday;
    private String gender; 

    // No-argument constructor
    public User() {
    }

    public User(String userName, String name, String birthday, String gender){
        this.userName = userName;
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
    }
}
