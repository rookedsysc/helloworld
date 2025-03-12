package org.rookedsysc.mybatishexagonalexam.auth.adapter.out;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.mybatishexagonalexam.auth.adapter.out.entity.UserEntity;
import org.rookedsysc.mybatishexagonalexam.auth.application.port.out.SignUpPort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuthAdapter implements SignUpPort {

    private final UserMapper userMapper;

    @Override
    public UserEntity signUp(UserEntity userEntity) {
        Long id = userMapper.save(userEntity);
        userEntity.setId(id);
        return userEntity;
    }
}
