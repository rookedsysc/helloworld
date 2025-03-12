package org.rookedsysc.mybatishexagonalexam.auth.application.port.out;

import org.rookedsysc.mybatishexagonalexam.auth.adapter.out.entity.UserEntity;

public interface SignUpPort {
    UserEntity signUp(UserEntity userEntity);
}
