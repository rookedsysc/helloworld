package org.rookedsysc.mybatishexagonalexam.post.adapter.converter.out;

import org.rookedsysc.mybatishexagonalexam.post.adapter.out.entity.PostEntity;
import org.rookedsysc.mybatishexagonalexam.post.application.port.in.PostCommand;

public class PostEntityConverter {
    public static PostEntity toEntity(PostCommand command) {
        return PostEntity.builder()
            .userId(command.userId())
            .title(command.title())
            .content(command.content())
            .build();
    }
}
