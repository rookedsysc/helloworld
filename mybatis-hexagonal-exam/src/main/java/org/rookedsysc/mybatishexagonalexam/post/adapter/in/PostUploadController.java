package org.rookedsysc.mybatishexagonalexam.post.adapter.in;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.mybatishexagonalexam.post.application.port.in.PostCommand;
import org.rookedsysc.mybatishexagonalexam.post.application.port.in.PostUploadUsecase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostUploadController {

    private final PostUploadUsecase postUploadUsecase;

    @PostMapping
    public long upload(@RequestBody PostCommand command) {
        return postUploadUsecase.upload(command);
    }
}
