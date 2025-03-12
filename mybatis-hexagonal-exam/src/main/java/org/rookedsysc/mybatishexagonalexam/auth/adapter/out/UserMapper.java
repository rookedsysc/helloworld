package org.rookedsysc.mybatishexagonalexam.auth.adapter.out;

import org.apache.ibatis.annotations.Mapper;
import org.rookedsysc.mybatishexagonalexam.auth.adapter.out.entity.UserEntity;

@Mapper
public interface UserMapper {
    long save(UserEntity userEntity);

    UserEntity findByEmail(String email);
}
