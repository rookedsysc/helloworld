from djangoninja.controller.dto.post_command import PostCommand
from djangoninja.controller.dto.post_response import PostResponse
from djangoninja.service.post_service import PostService
from ninja import Router

router = Router()

post_service = PostService()

@router.post("")
def create_posts(request, post_command: PostCommand) -> PostResponse :
    response = post_service.create(post_command)
    return response
  