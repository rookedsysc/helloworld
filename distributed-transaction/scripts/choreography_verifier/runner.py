import time
from typing import Any

from choreography_verifier.api import ChoreographyApi, VerificationError
from choreography_verifier.cases import VerificationCase


FINAL_STATUSES = {"COMPLETED", "FAILED"}


def run_case(api: ChoreographyApi, case: VerificationCase, user_id: int) -> None:
    products = [
        api.create_product(
            quantity=product.product_quantity,
            price=product.product_price,
        )
        for product in case.products
    ]
    api.create_point(user_id=user_id, amount=case.point_amount)

    order = api.create_order(
        user_id=user_id,
        items=[
            {
                "productId": created_product["id"],
                "quantity": product.order_quantity,
            }
            for created_product, product in zip(products, case.products)
        ],
    )
    order_id = order["orderId"]

    api.place_order(order_id)
    order_result = wait_for_status(api, order_id, case.expected_status)

    actual_products = [
        api.get_product(product["id"])
        for product in products
    ]
    actual_point = api.get_point(user_id)

    assert_equal(
        f"{case.name} order status",
        order_result["status"],
        case.expected_status,
    )
    for index, (actual_product, expected_quantity) in enumerate(
        zip(actual_products, case.expected_product_quantities()),
        start=1,
    ):
        assert_equal(
            f"{case.name} product[{index}] quantity",
            actual_product["quantity"],
            expected_quantity,
        )
    assert_equal(
        f"{case.name} point amount",
        actual_point["amount"],
        case.expected_point_amount(),
    )
    print_case_report(
        case=case,
        user_id=user_id,
        products=products,
        order_id=order_id,
        order_result=order_result,
        actual_products=actual_products,
        actual_point=actual_point,
    )


def wait_for_status(
    api: ChoreographyApi,
    order_id: int,
    expected_status: str,
    timeout_seconds: float = 20.0,
    interval_seconds: float = 0.5,
) -> dict[str, Any]:
    deadline = time.monotonic() + timeout_seconds
    last_result = None

    while time.monotonic() < deadline:
        last_result = api.get_order_result(order_id)
        last_status = last_result["status"]

        if last_status == expected_status:
            return last_result

        if last_status in FINAL_STATUSES:
            raise VerificationError(
                f"order {order_id} status is {last_status}, expected {expected_status}"
            )

        time.sleep(interval_seconds)

    raise VerificationError(
        f"order {order_id} status timeout: last={last_result}, expected={expected_status}"
    )


def assert_equal(label: str, actual: int | str, expected: int | str) -> None:
    if actual != expected:
        raise VerificationError(f"{label}: actual={actual}, expected={expected}")


def print_case_report(
    case: VerificationCase,
    user_id: int,
    products: list[dict[str, Any]],
    order_id: int,
    order_result: dict[str, Any],
    actual_products: list[dict[str, Any]],
    actual_point: dict[str, Any],
) -> None:
    print()
    print(f"[{case.name}] PASS")
    print(
        "  ids       "
        f"product_ids={format_product_ids(products)}, user_id={user_id}, order_id={order_id}"
    )
    print("  input")
    for index, product in enumerate(case.products, start=1):
        print(
            f"    - product[{index}] "
            f"stock={product.product_quantity}, order_quantity={product.order_quantity}, "
            f"unit_price={product.product_price}, subtotal={product.total_price()}"
        )
    print(
        "    - payment "
        f"point={case.point_amount}, "
        f"total_price={case.total_price()}"
    )
    print("  basis")
    for line in case.basis_lines():
        print(f"    - {line}")
    print("  checks")
    print_check("order.status", order_result["status"], case.expected_status)
    for index, (actual_product, expected_quantity) in enumerate(
        zip(actual_products, case.expected_product_quantities()),
        start=1,
    ):
        print_check(
            f"product[{index}].quantity",
            actual_product["quantity"],
            expected_quantity,
        )
    print_check("point.amount", actual_point["amount"], case.expected_point_amount())


def print_check(label: str, actual: int | str, expected: int | str) -> None:
    result = "OK" if actual == expected else "FAIL"
    print(f"    - {label:<20} actual={actual:<10} expected={expected:<10} {result}")


def format_product_ids(products: list[dict[str, Any]]) -> str:
    return "[" + ", ".join(str(product["id"]) for product in products) + "]"
