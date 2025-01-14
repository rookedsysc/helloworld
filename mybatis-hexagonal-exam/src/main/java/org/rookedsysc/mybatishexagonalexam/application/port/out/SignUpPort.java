package org.rookedsysc.mybatishexagonalexam.application.port.out;

import org.rookedsysc.mybatishexagonalexam.adapter.out.entity.UserEntity;

public interface SignUpPort {
    UserEntity signUp(UserEntity userEntity);
}
