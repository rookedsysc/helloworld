package org.rookedsysc.mybatishexagonalexam.auth.adapter.converter.out;

import org.rookedsysc.mybatishexagonalexam.auth.adapter.out.entity.UserEntity;
import org.rookedsysc.mybatishexagonalexam.auth.application.port.in.AuthCommand;

public class AuthEntityConverter {
    public static UserEntity toEntity(AuthCommand command) {
        return UserEntity.builder()
            .email(command.email())
            .password(command.password())
            .build();
    }
}
