package org.rookedsysc.mybatishexagonalexam.application.port.in;

public record PostCommand(
    Long userId,
    String title,
    String content
) {
}
