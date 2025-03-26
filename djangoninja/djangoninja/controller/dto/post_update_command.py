from typing import Optional
from pydantic import BaseModel

class PostUpdateCommand(BaseModel) : 
  title: Optional[str] = None
  content: Optional[str] = None