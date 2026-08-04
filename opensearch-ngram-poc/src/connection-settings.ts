/** MySQL 접속 정보. 로컬 docker-compose 기본값을 쓰며 환경변수로 덮어쓸 수 있다. */
export const MYSQL_CONNECTION = {
  host: process.env.MYSQL_HOST ?? '127.0.0.1',
  port: Number(process.env.MYSQL_PORT ?? 3306),
  user: process.env.MYSQL_USER ?? 'root',
  password: process.env.MYSQL_PASSWORD ?? '12345678',
  database: process.env.MYSQL_DATABASE ?? 'novel_search',
};

export const OPENSEARCH_NODE = process.env.OPENSEARCH_NODE ?? 'http://127.0.0.1:9200';

/** 두 엔진이 같은 이름을 쓰도록 고정한다. */
export const NOVEL_TABLE_NAME = 'novel';
export const NOVEL_INDEX_NAME = 'novel';

/** 적재할 문서 수. 100만건으로 올리려면 DOC_COUNT만 바꾸면 된다. */
export const DOCUMENT_COUNT = Number(process.env.DOC_COUNT ?? 100000);

/** 두 엔진에 같은 본문을 넣기 위한 생성 시드. */
export const GENERATOR_SEED = Number(process.env.GENERATOR_SEED ?? 42);

export const HTTP_PORT = Number(process.env.PORT ?? 3000);
