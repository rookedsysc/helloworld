package org.rookedsysc.mybatishexagonalexam.adapter.out;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.mybatishexagonalexam.adapter.out.entity.PostEntity;
import org.rookedsysc.mybatishexagonalexam.application.port.out.PostUploadPort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostAdapter implements PostUploadPort {

    private final PostMapper postMapper;

    @Override
    public long upload(PostEntity entity) {
        postMapper.save(entity);

        long lastUploadedId = postMapper.findLastPostByUserId(entity.getUserId());

        return lastUploadedId;
    }
}
