package org.rookedsysc.cqrsreplicarag.post;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public Post createPost(String title, String content) {
        return postService.createPost(title, content);
    }

    @GetMapping("/count")
    public int countAllPosts() {
        return postService.countAllPosts();
    }
}
