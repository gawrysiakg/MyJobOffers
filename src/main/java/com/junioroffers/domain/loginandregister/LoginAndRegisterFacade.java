package com.junioroffers.domain.loginandregister;

import com.junioroffers.domain.loginandregister.dto.RegisterUserDto;
import com.junioroffers.domain.loginandregister.dto.RegistrationResultDto;
import com.junioroffers.domain.loginandregister.dto.UserDto;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class LoginAndRegisterFacade {

    private final LoginRepository repository;
    public static final String USER_NOT_FOUND="User not found";

    public UserDto findByUsername(String username){
        return repository.findByUsername(username)
                .map(user->new UserDto(user.id(), user.password(), user.username()))
                .orElseThrow(()-> new UsernameNotFoundException(USER_NOT_FOUND)); // <--exception delivered with spring (UserDetails)
    }

    public RegistrationResultDto register(RegisterUserDto registerUserDto){
            final User user=User.builder()
                    .username(registerUserDto.username())
                    .password(registerUserDto.password())
                    .build();
            User savedUser = repository.save(user);
            return new RegistrationResultDto(savedUser.id(), true, savedUser.username());
    }
}
