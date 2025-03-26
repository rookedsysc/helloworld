import datetime

class Post():
  def __init__(self, id, title, content, created_at, updated_at):
    self.id = id
    self.title = title
    self.content = content
    self.created_at = created_at
    self.updated_at = updated_at
    