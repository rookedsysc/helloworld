package org.rookedsysc.mybatishexagonalexam.post.adapter.out.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PostEntity {
    private Long id;
    private Long userId;
    private String title;
    private String content;

    @Builder
    public PostEntity(Long userId, String title, String content) {
        this.title = title;
        this.content = content;
    }
}
