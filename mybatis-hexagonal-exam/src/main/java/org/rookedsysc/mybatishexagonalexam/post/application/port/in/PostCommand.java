package org.rookedsysc.mybatishexagonalexam.post.application.port.in;

public record PostCommand(
    Long userId,
    String title,
    String content
) {
}
