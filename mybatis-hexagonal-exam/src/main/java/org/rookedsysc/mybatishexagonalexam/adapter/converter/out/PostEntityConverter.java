package org.rookedsysc.mybatishexagonalexam.adapter.converter.out;

import org.rookedsysc.mybatishexagonalexam.adapter.out.entity.PostEntity;
import org.rookedsysc.mybatishexagonalexam.application.port.in.PostCommand;

public class PostEntityConverter {
    public static PostEntity toEntity(PostCommand command) {
        return PostEntity.builder()
            .userId(command.userId())
            .title(command.title())
            .content(command.content())
            .build();
    }
}
