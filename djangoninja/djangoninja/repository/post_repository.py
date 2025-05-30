from djangoninja.entity.post import Post
from djangoninja.repository.db_connection import DatabaseConnection

class PostRepository :
  def create_post(self, post: Post) -> Post : 
    db = DatabaseConnection()
    connection = db.get_connection()
    cursor = connection.cursor()
    
    query = "INSERT INTO djangoninja_post (title, content, created_at, updated_at) VALUES (%s, %s, %s, %s)"
    cursor.execute(query, (post.title, post.content, post.created_at, post.updated_at))
    connection.commit()
    
    post_id = cursor.lastrowid  # 삽입된 레코드의 ID 가져오기
    post.id = post_id
    
    cursor.close()
    connection.close()
    
    
    return post
  
  def get_all(self) -> list[Post] :
    db = DatabaseConnection()
    connection = db.get_connection()
    cursor = connection.cursor()
    
    query = "SELECT * FROM djangoninja_post"
    cursor.execute(query)
    
    posts = []
    for (id, title, content, created_at, updated_at) in cursor.fetchall():
      post = Post(id=id, title=title, content=content, created_at=created_at, updated_at=updated_at)
      posts.append(post)
    
    cursor.close()
    connection.close()
    
    return posts
  
  def get_detail(self, id) -> Post :
    db = DatabaseConnection()
    connection = db.get_connection()
    cursor = connection.cursor()
    
    query = "SELECT * FROM djangoninja_post WHERE id=%s"
    cursor.execute(query, (id,))
    
    post = None
    for (id, title, content, created_at, updated_at) in cursor.fetchall():
      post = Post(id=id, title=title, content=content, created_at=created_at, updated_at=updated_at)
    
    cursor.close()
    connection.close()
    
    return post

  def update(self, post: Post) -> None :
    db = DatabaseConnection()
    connection = db.get_connection()
    cursor = connection.cursor()

    fields = []
    values = []

    if post.title is not None:
      fields.append("title=%s")
      values.append(post.title)
    if post.content is not None:
      fields.append("content=%s")
      values.append(post.content)
    if post.updated_at is not None:
      fields.append("updated_at=%s")
      values.append(post.updated_at)

    values.append(post.id)

    query = f"UPDATE djangoninja_post SET {', '.join(fields)} WHERE id=%s"
    cursor.execute(query, values)
    connection.commit()

    cursor.close()
    connection.close()
  
  def delete(self, id: int) -> None : 
    db = DatabaseConnection()
    connection = db.get_connection()
    cursor = connection.cursor()
    
    query = "DELETE FROM djangoninja_post WHERE id=%s"
    cursor.execute(query, (id,))
    connection.commit()
    
    cursor.close()
    connection.close()
    
  @staticmethod
  def ensure_post_table_exists():
    db = DatabaseConnection()
    connection = db.get_connection()
    cursor = connection.cursor()
    create_table_query = """
    CREATE TABLE IF NOT EXISTS djangoninja_post (
      id INT AUTO_INCREMENT PRIMARY KEY,
      title VARCHAR(255) NOT NULL,
      content TEXT NOT NULL,
      created_at DATETIME NOT NULL,
      updated_at DATETIME NOT NULL
    )
    """
    cursor.execute(create_table_query)
    cursor.close()
    connection.close()