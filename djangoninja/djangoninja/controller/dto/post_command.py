from venv import create
from pydantic import BaseModel

class PostCommand(BaseModel) :
  title: str
  content: str