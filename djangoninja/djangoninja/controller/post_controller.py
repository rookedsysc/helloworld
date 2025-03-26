from re import A
from djangoninja.controller.dto.post_command import PostCommand
from djangoninja.controller.dto.post_response import PostResponse
from djangoninja.controller.dto.post_update_command import PostUpdateCommand
from djangoninja.service.post_service import PostService
from ninja import Router

router = Router()

post_service = PostService()
  
@router.post("")
def create_posts(request, post_command: PostCommand) -> PostResponse :
    response = post_service.create(post_command)
    return response

@router.get("")
def get_all(request) -> PostResponse :
    return post_service.get_all()
  
@router.get("/{id}")
def get_detail(request, id: int) -> PostResponse :
    return post_service.get_detail(id)
  
@router.put("/{id}")
def update_post(request, id: int, post_command: PostUpdateCommand) -> None :
    post_service.update(id, post_command)
    
@router.delete("/{id}")
def delete_post(request, id: int) -> None :
    post_service.delete(id)