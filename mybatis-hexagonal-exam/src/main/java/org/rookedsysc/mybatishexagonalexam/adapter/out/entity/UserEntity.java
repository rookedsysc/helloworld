package org.rookedsysc.mybatishexagonalexam.adapter.out.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserEntity {
    private Long id;
    private String email;
    private String password;

    @Builder
    public UserEntity(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
