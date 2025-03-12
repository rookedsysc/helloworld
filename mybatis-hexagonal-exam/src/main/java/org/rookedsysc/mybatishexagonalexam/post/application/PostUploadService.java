package org.rookedsysc.mybatishexagonalexam.post.application;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.mybatishexagonalexam.post.adapter.converter.out.PostEntityConverter;
import org.rookedsysc.mybatishexagonalexam.post.adapter.out.entity.PostEntity;
import org.rookedsysc.mybatishexagonalexam.post.application.port.in.PostCommand;
import org.rookedsysc.mybatishexagonalexam.post.application.port.in.PostUploadUsecase;
import org.rookedsysc.mybatishexagonalexam.post.application.port.out.PostUploadPort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class PostUploadService implements PostUploadUsecase {

    private final PostUploadPort postUploadPort;
    private final RedisTemplate<String, Integer> redisTemplate;
    private final TransactionTemplate transactionTemplate;

    @Override
    public long upload(PostCommand command) {
        return transactionTemplate.execute(status -> {
            try {
                PostEntity entity = PostEntityConverter.toEntity(command);

                redisTemplate.opsForValue().setIfAbsent("post", 0);
                redisTemplate.opsForValue().increment("post", 1);

                return postUploadPort.upload(entity);
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException(e);
            }
        });
    }
}
