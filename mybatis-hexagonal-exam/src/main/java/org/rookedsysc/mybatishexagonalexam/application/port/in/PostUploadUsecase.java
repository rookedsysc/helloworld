package org.rookedsysc.mybatishexagonalexam.application.port.in;

import org.rookedsysc.mybatishexagonalexam.adapter.out.entity.PostEntity;

public interface PostUploadUsecase {
    /**
     * 업로드 한 포스트의 ID를 반환
     */
    long upload(PostCommand command);
}
