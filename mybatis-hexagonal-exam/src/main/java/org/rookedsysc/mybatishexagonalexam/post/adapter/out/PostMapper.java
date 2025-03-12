package org.rookedsysc.mybatishexagonalexam.post.adapter.out;

import org.apache.ibatis.annotations.Mapper;
import org.rookedsysc.mybatishexagonalexam.post.adapter.out.entity.PostEntity;

@Mapper
public interface PostMapper {
    long save(PostEntity post);
}
