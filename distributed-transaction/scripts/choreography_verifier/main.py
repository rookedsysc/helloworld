import time

import httpx

from choreography_verifier.api import ChoreographyApi, load_service_urls
from choreography_verifier.cases import verification_cases
from choreography_verifier.runner import run_case


def main() -> None:
    urls = load_service_urls()
    user_id_base = time.time_ns() // 1_000_000

    with httpx.Client(timeout=5.0) as client:
        api = ChoreographyApi(urls=urls, client=client)
        for index, case in enumerate(verification_cases(), start=1):
            run_case(api, case, user_id=user_id_base + index)
