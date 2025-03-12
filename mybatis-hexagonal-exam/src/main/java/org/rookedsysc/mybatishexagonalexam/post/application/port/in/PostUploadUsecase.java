package org.rookedsysc.mybatishexagonalexam.post.application.port.in;

public interface PostUploadUsecase {
    /**
     * 업로드 한 포스트의 ID를 반환
     */
    long upload(PostCommand command);
}
