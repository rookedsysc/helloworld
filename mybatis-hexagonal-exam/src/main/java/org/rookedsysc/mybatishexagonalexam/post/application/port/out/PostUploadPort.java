package org.rookedsysc.mybatishexagonalexam.post.application.port.out;

import org.rookedsysc.mybatishexagonalexam.post.adapter.out.entity.PostEntity;

public interface PostUploadPort {
    long upload(PostEntity command);
}
