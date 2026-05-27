from dataclasses import dataclass
from typing import Any

import httpx


class VerificationError(Exception):
    pass


@dataclass(frozen=True)
class ServiceUrls:
    product: str
    order: str
    point: str


def load_service_urls() -> ServiceUrls:
    return ServiceUrls(
        product="http://localhost:9785",
        order="http://localhost:9786",
        point="http://localhost:9787",
    )


class ChoreographyApi:
    def __init__(self, urls: ServiceUrls, client: httpx.Client):
        self.urls = urls
        self.client = client

    def create_product(self, quantity: int, price: int) -> dict[str, Any]:
        return self._request_json(
            "POST",
            f"{self.urls.product}/products",
            {"quantity": quantity, "price": price},
        )

    def get_product(self, product_id: int) -> dict[str, Any]:
        return self._request_json("GET", f"{self.urls.product}/products/{product_id}")

    def create_point(self, user_id: int, amount: int) -> dict[str, Any]:
        return self._request_json(
            "POST",
            f"{self.urls.point}/points",
            {"userId": user_id, "amount": amount},
        )

    def get_point(self, user_id: int) -> dict[str, Any]:
        return self._request_json("GET", f"{self.urls.point}/points/{user_id}")

    def create_order(self, user_id: int, items: list[dict[str, int]]) -> dict[str, Any]:
        return self._request_json(
            "POST",
            f"{self.urls.order}/orders",
            {
                "userId": user_id,
                "items": items,
            },
        )

    def place_order(self, order_id: int) -> None:
        self._request_json(
            "POST",
            f"{self.urls.order}/orders/choreography/place",
            {"orderId": order_id},
        )

    def get_order_result(self, order_id: int) -> dict[str, Any]:
        return self._request_json("GET", f"{self.urls.order}/orders/{order_id}/result")

    def _request_json(
        self,
        method: str,
        url: str,
        payload: dict[str, Any] | None = None,
    ) -> Any:
        headers = {"accept": "application/json"}
        if payload is not None:
            headers["Content-Type"] = "application/json"

        try:
            response = self.client.request(method, url, headers=headers, json=payload)
        except httpx.HTTPError as error:
            raise VerificationError(f"{method} {url} failed: {error}") from error

        if response.is_error:
            raise VerificationError(
                f"{method} {url} failed: {response.status_code} {response.text}"
            )

        if not response.content:
            return None

        return response.json()
