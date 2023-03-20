package com.junioroffers.domain.loginandregister;

import com.junioroffers.domain.loginandregister.dto.RegisterUserDto;
import com.junioroffers.domain.loginandregister.dto.RegistrationResultDto;
import com.junioroffers.domain.loginandregister.dto.UserDto;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
class LoginAndRegisterFacadeTest {

    LoginRepository repository = new LoginRepositoryTestImpl();
    LoginAndRegisterFacade loginAndRegisterFacade = new LoginAndRegisterFacade(repository);


    //podejście ditroid testuje fasadę a nie każdą metodę i klasę oddzielnie, mniej kodu,
    // nie trzeba mockować wszystkiego z klasy zależnej(black box testing)

    @Test
    void should_throw_exception_when_user_not_found(){
        //Given
        String username = "exemplarUser";
        //When
        Throwable throwing = catchThrowable(()-> loginAndRegisterFacade.findByUsername(username));
        //Then
        AssertionsForClassTypes.assertThat(throwing)
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }


    @Test
    void should_find_user_by_username(){
        //Given
        RegisterUserDto registerUserDto = new RegisterUserDto("Username", "Password");
        RegistrationResultDto  registrationResultDto = loginAndRegisterFacade.register(registerUserDto);
        //When
        UserDto fromRepository = loginAndRegisterFacade.findByUsername(registrationResultDto.username());
        //Then
        assertThat(fromRepository).isEqualTo(new UserDto(registrationResultDto.id(),"Password", "Username" ));
    }


    @Test
    void should_register_user(){
        //Given
        RegisterUserDto registerUserDto = new RegisterUserDto("username1", "password1");
        //When
        RegistrationResultDto resultDto = loginAndRegisterFacade.register(registerUserDto);
        //Then
        assertAll(
                ()->assertThat(resultDto.created()).isTrue(),
                ()->assertThat(resultDto.username()).isEqualTo("username1")
        );
    }
}