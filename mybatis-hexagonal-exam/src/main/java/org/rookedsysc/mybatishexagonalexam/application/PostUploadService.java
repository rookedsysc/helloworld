package org.rookedsysc.mybatishexagonalexam.application;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.mybatishexagonalexam.adapter.converter.out.PostEntityConverter;
import org.rookedsysc.mybatishexagonalexam.adapter.out.entity.PostEntity;
import org.rookedsysc.mybatishexagonalexam.application.port.in.PostCommand;
import org.rookedsysc.mybatishexagonalexam.application.port.in.PostUploadUsecase;
import org.rookedsysc.mybatishexagonalexam.application.port.out.PostUploadPort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostUploadService implements PostUploadUsecase {

    private final PostUploadPort postUploadPort;

    @Override
    public long upload(PostCommand command) {
        PostEntity entity = PostEntityConverter.toEntity(command)
        return postUploadPort.upload(entity);
    }
}
