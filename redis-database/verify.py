import subprocess
from pathlib import Path

PROJECT_DIRECTORY = Path(__file__).resolve().parent
COMPOSE_FILE = PROJECT_DIRECTORY / "compose.yaml"


def run_compose(*arguments: str) -> str:
    command = [
        "docker",
        "compose",
        "--project-directory",
        str(PROJECT_DIRECTORY),
        "-f",
        str(COMPOSE_FILE),
        *arguments,
    ]
    completed_process = subprocess.run(
        command,
        check=True,
        capture_output=True,
        text=True,
    )
    return completed_process.stdout.strip()


def run_redis(*arguments: str, database: int | None = None) -> str:
    command = ["exec", "-T", "redis", "redis-cli", "--raw"]
    if database is not None:
        command.extend(["-n", str(database)])

    return run_compose(*command, *arguments)


def assert_equal(expected: str, actual: str, description: str) -> None:
    if actual != expected:
        raise AssertionError(
            f"FAIL: {description} (expected={expected}, actual={actual})"
        )

    print(f"PASS: {description}")


def verify_same_key_is_isolated_by_database() -> None:
    run_redis("SET", "example:key", "db-0", database=0)
    run_redis("SET", "example:key", "db-1", database=1)

    assert_equal(
        "db-0",
        run_redis("GET", "example:key", database=0),
        "같은 키 이름을 DB 0에서 독립적으로 조회",
    )
    assert_equal(
        "db-1",
        run_redis("GET", "example:key", database=1),
        "같은 키 이름을 DB 1에서 독립적으로 조회",
    )


def verify_flushdb_only_clears_selected_database() -> None:
    run_redis("SET", "flush:key", "db-0", database=0)
    run_redis("SET", "flush:key", "db-1", database=1)
    run_redis("FLUSHDB", database=1)

    assert_equal(
        "db-0",
        run_redis("GET", "flush:key", database=0),
        "FLUSHDB가 다른 DB의 키를 유지",
    )
    assert_equal(
        "",
        run_redis("GET", "flush:key", database=1),
        "FLUSHDB가 선택한 DB만 비움",
    )


def verify_swapdb_exchanges_datasets() -> None:
    run_redis("SET", "active-dataset", "blue", database=0)
    run_redis("SET", "active-dataset", "green", database=1)
    run_redis("SWAPDB", "0", "1")

    assert_equal(
        "green",
        run_redis("GET", "active-dataset", database=0),
        "SWAPDB가 DB 0의 데이터셋을 교환",
    )
    assert_equal(
        "blue",
        run_redis("GET", "active-dataset", database=1),
        "SWAPDB가 DB 1의 데이터셋을 교환",
    )


def get_eviction_policy(database: int) -> str:
    config_response = run_redis(
        "CONFIG",
        "GET",
        "maxmemory-policy",
        database=database,
    )
    return config_response.splitlines()[-1]


def verify_eviction_policy_is_shared() -> None:
    original_policy = get_eviction_policy(database=0)

    try:
        run_redis(
            "CONFIG",
            "SET",
            "maxmemory-policy",
            "allkeys-lru",
            database=1,
        )
        assert_equal(
            "allkeys-lru",
            get_eviction_policy(database=0),
            "DB 1에서 바꾼 eviction 정책을 DB 0도 공유",
        )
        assert_equal(
            "allkeys-lru",
            get_eviction_policy(database=1),
            "eviction 정책은 logical DB별 설정이 아님",
        )
    finally:
        run_redis("CONFIG", "SET", "maxmemory-policy", original_policy)


def main() -> None:
    print("Redis 컨테이너를 시작합니다.")
    run_compose("up", "-d", "--wait")
    run_redis("FLUSHALL")

    verify_same_key_is_isolated_by_database()
    verify_flushdb_only_clears_selected_database()
    verify_swapdb_exchanges_datasets()
    verify_eviction_policy_is_shared()

    print("Redis logical database PoC 검증을 완료했습니다.")


if __name__ == "__main__":
    main()
