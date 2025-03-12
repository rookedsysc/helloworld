package org.rookedsysc.mybatishexagonalexam.auth.application.port.in;

import org.rookedsysc.mybatishexagonalexam.auth.adapter.out.entity.UserEntity;

public interface SignUpUseCase {
    UserEntity signUp(AuthCommand command);
}
