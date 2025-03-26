import mysql.connector
from mysql.connector import pooling

# Database Pool을 싱글톤으로 관리하기 위한 객체
class DatabaseConnection:
  _instance = None
  _connection_pool = None

  def __new__(cls):
    if cls._instance is None:
      cls._instance = super(DatabaseConnection, cls).__new__(cls)
      cls._initialize_connection_pool()
    return cls._instance

  @classmethod
  def _initialize_connection_pool(cls):
    cls._connection_pool = pooling.MySQLConnectionPool(
      pool_name="mypool",
      pool_size=10,
      pool_reset_session=True,
      host='localhost',
      database='django',
      user='root',
      password='1234'
    )

  def get_connection(self):
    return self._connection_pool.get_connection()