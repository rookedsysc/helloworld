package org.rookedsysc.mybatishexagonalexam.application.port.in;

import org.rookedsysc.mybatishexagonalexam.adapter.out.entity.UserEntity;

public interface SignUpUseCase {
    UserEntity signUp(AuthCommand command);
}
