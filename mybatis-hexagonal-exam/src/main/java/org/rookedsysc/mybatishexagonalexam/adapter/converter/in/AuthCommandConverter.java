package org.rookedsysc.mybatishexagonalexam.adapter.converter.in;

import org.rookedsysc.mybatishexagonalexam.adapter.in.AuthRequest;
import org.rookedsysc.mybatishexagonalexam.application.port.in.AuthCommand;

public class AuthCommandConverter {
    public static AuthCommand toCommand(AuthRequest request) {
        return AuthCommand.builder()
            .email(request.email())
            .password(request.password())
            .build();
    }
}
