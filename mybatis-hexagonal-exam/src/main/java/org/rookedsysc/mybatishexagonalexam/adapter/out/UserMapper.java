package org.rookedsysc.mybatishexagonalexam.adapter.out;

import org.apache.ibatis.annotations.Mapper;
import org.rookedsysc.mybatishexagonalexam.adapter.out.entity.UserEntity;

@Mapper
public interface UserMapper {
    int save(UserEntity userEntity);

    UserEntity findByEmail(String email);
}
