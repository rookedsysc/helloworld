export const routes = {
  login: "/login",
  posts: "/posts",
  createPost: "/posts/new",
  postDetail: (postId: number) => `/posts/${postId}`,
} as const;
