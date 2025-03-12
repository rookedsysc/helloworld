package org.rookedsysc.mybatishexagonalexam.auth.adapter.in;

public record AuthRequest(
    String email,
    String password
) {
}
