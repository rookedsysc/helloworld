package org.rookedsysc.mybatishexagonalexam.auth.adapter.out.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
public class UserEntity {
    @Setter
    private Long id;
    private String email;
    private String password;

    @Builder
    public UserEntity(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
