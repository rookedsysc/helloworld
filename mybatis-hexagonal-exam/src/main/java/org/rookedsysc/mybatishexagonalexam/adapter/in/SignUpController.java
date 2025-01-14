package org.rookedsysc.mybatishexagonalexam.adapter.in;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.mybatishexagonalexam.adapter.converter.in.AuthCommandConverter;
import org.rookedsysc.mybatishexagonalexam.adapter.out.entity.UserEntity;
import org.rookedsysc.mybatishexagonalexam.application.port.in.AuthCommand;
import org.rookedsysc.mybatishexagonalexam.application.port.in.SignUpUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class SignUpController {

    private final SignUpUseCase signUpUseCase;

    @PostMapping("/signup")
    public UserEntity signUp(@RequestBody AuthRequest request) {
        AuthCommand command = AuthCommandConverter.toCommand(request);
        return signUpUseCase.signUp(command);
    }
}
