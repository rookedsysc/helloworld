package org.rookedsysc.mybatishexagonalexam.post.application;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.mybatishexagonalexam.post.adapter.converter.out.PostEntityConverter;
import org.rookedsysc.mybatishexagonalexam.post.adapter.out.entity.PostEntity;
import org.rookedsysc.mybatishexagonalexam.post.application.port.in.PostCommand;
import org.rookedsysc.mybatishexagonalexam.post.application.port.in.PostUploadUsecase;
import org.rookedsysc.mybatishexagonalexam.post.application.port.out.PostUploadPort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostUploadService implements PostUploadUsecase {

    private final PostUploadPort postUploadPort;

    @Override
    public long upload(PostCommand command) {
        PostEntity entity = PostEntityConverter.toEntity(command);
        return postUploadPort.upload(entity);
    }
}
