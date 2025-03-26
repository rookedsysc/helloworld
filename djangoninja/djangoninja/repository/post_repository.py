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