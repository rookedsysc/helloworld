package org.rookedsysc.mybatishexagonalexam.adapter.out;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.mybatishexagonalexam.adapter.out.entity.UserEntity;
import org.rookedsysc.mybatishexagonalexam.application.port.out.SignUpPort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuthAdapter implements SignUpPort {

    private final UserMapper userMapper;

    @Override
    public UserEntity signUp(UserEntity userEntity) {
        userMapper.save(userEntity);
        userEntity = userMapper.findByEmail(userEntity.getEmail());
        return userEntity;
    }
}
