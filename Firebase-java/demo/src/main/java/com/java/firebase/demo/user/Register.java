package com.java.firebase.demo.user;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Register {
    @Nonnull
    private String userName;
    @Nonnull
    private String name;
    @Nonnull
    private String birthday;
    @Nonnull
    private String email;
    @Nonnull
    private String gender;
    @Nonnull 
    private String password;
}
