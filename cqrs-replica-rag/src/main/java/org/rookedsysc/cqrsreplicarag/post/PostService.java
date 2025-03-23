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
    public Post createPost(String title, String content) {
        Post post = Post.builder().title(title).content(content).build();
        return postRepository.save(post);
    }

    @QueryService
    public int countAllPosts() {
        return postRepository.countAllBy();
    }
}
