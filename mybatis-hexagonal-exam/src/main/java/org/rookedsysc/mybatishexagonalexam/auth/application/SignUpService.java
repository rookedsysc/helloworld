package org.rookedsysc.mybatishexagonalexam.auth.application;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.mybatishexagonalexam.auth.adapter.converter.out.AuthEntityConverter;
import org.rookedsysc.mybatishexagonalexam.auth.adapter.out.entity.UserEntity;
import org.rookedsysc.mybatishexagonalexam.auth.application.port.in.AuthCommand;
import org.rookedsysc.mybatishexagonalexam.auth.application.port.in.SignUpUseCase;
import org.rookedsysc.mybatishexagonalexam.auth.application.port.out.SignUpPort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignUpService implements SignUpUseCase {

    private final SignUpPort signUpPort;

    @Override
    public UserEntity signUp(AuthCommand command) {
        UserEntity userEntity = AuthEntityConverter.toEntity(command);
        userEntity = signUpPort.signUp(userEntity);
        return userEntity;
    }
}
