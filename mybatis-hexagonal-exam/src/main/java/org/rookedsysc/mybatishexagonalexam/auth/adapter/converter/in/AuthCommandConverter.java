package org.rookedsysc.mybatishexagonalexam.auth.adapter.converter.in;

import org.rookedsysc.mybatishexagonalexam.auth.adapter.in.AuthRequest;
import org.rookedsysc.mybatishexagonalexam.auth.application.port.in.AuthCommand;

public class AuthCommandConverter {
    public static AuthCommand toCommand(AuthRequest request) {
        return AuthCommand.builder()
            .email(request.email())
            .password(request.password())
            .build();
    }
}
