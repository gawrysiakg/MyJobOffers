package com.junioroffers.domain.loginandregister;

public class UsernameNotFoundException extends RuntimeException {
    public UsernameNotFoundException(String userNotFound){
        super(userNotFound);
    }
}
