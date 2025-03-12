package org.rookedsysc.mybatishexagonalexam.auth.application.port.in;

import lombok.Builder;

@Builder
public record AuthCommand(
    String email,
    String password
) {
}
