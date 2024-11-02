package com.java.firebase.demo.user;

import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserCredentials {
    @Nonnull
    private String email;
    @Nonnull
    private String password;
}
