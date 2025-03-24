package org.rookedsysc.cqrsreplicarag.post;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostWriteResponse {
    private Long id;
    private String title;
    private String content;
    private int count;
}
