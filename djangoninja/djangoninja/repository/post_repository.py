from djangoninja.entity.post import Post
from djangoninja.repository.db_connection import DatabaseConnection
class PostRepository :
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