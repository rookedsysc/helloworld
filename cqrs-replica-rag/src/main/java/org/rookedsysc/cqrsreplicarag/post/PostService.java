package org.rookedsysc.cqrsreplicarag.post;

import lombok.RequiredArgsConstructor;
import org.rookedsysc.cqrsreplicarag.config.CommandService;
import org.rookedsysc.cqrsreplicarag.config.QueryService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    @CommandService
    public PostWriteResponse createPost(String title, String content) {
        Post post = Post.builder().title(title).content(content).build();

        int count = postRepository.countAllBy();

        PostWriteResponse response = PostWriteResponse.builder()
            .id(postRepository.save(post).getId())
            .title(title)
            .content(content)
            .count(count)
            .build();
        return response;
    }

    @QueryService
    public int countAllPosts() {
        return postRepository.countAllBy();
    }
}
