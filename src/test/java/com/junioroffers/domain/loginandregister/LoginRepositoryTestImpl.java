package com.junioroffers.domain.loginandregister;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LoginRepositoryTestImpl implements LoginRepository{

    private final Map<String, User> loginList = new ConcurrentHashMap<>();
    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(loginList.get(username));
    }

//    @Override
//    public User save(User user) {
//        return loginList.put(user.username(), user);
//    }

    @Override
    public User save(User entity) {
        UUID id = UUID.randomUUID();
        User user = new User(
                id.toString(),
                entity.username(),
                entity.password()
        );
        loginList.put(user.username(), user);
        return user;
    }
}
