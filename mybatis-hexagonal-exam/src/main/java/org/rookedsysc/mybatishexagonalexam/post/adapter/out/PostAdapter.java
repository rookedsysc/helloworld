package org.rookedsysc.mybatishexagonalexam.post.adapter.out;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.mybatishexagonalexam.post.adapter.out.entity.PostEntity;
import org.rookedsysc.mybatishexagonalexam.post.application.port.out.PostUploadPort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostAdapter implements PostUploadPort {

    private final PostMapper postMapper;

    @Override
    public long upload(PostEntity entity) {
        long id = postMapper.save(entity);

        return id;
    }
}
