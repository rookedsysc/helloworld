from djangoninja.controller.dto.post_command import PostCommand
from djangoninja.controller.dto.post_response import PostResponse
from djangoninja.controller.dto.post_update_command import PostUpdateCommand
from djangoninja.entity.post import Post
from djangoninja.repository.db_connection import DatabaseConnection
from djangoninja.repository.post_repository import PostRepository
from datetime import datetime

class PostService:
  def __init__(self):
    self._post_repository = PostRepository()

  def create(self, post_command: PostCommand) -> PostResponse :
    post = Post(id=None, title=post_command.title, content=post_command.content, created_at=datetime.now(), updated_at=datetime.now())
    
    saved_post = self._post_repository.create_post(post)
    
    response = PostResponse(id=saved_post.id, title=saved_post.title, content=saved_post.content, created_at=saved_post.created_at, updated_at=saved_post.updated_at)
    
    return response 
  
  def get_all(self) -> list[PostResponse] :
    post_list: list[Post] = self._post_repository.get_all()
    response: list[PostResponse] = []
    for post in post_list : 
      response.append(PostResponse(id=post.id, title=post.title, content=post.content, created_at=post.created_at, updated_at=post.updated_at))
      
    return response
  
  def get_detail(self, id) -> PostResponse :
    post = self._post_repository.get_detail(id)
    response = PostResponse(id=post.id, title=post.title, content=post.content, created_at=post.created_at, updated_at=post.updated_at)
    return response
  
  def update(self, id: int, post_command: PostUpdateCommand) -> None :
    post = Post(id=id, title=post_command.title, content=post_command.content, created_at=None, updated_at=datetime.now())
    
    self._post_repository.update(post)