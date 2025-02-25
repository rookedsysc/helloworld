package org.rookedsysc.mybatishexagonalexam.application.port.out;

import org.rookedsysc.mybatishexagonalexam.adapter.out.entity.PostEntity;
import org.rookedsysc.mybatishexagonalexam.application.port.in.PostCommand;

public interface PostUploadPort {
    long upload(PostEntity command);
}
