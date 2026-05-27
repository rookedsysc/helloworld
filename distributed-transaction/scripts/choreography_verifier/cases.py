from dataclasses import dataclass


@dataclass(frozen=True)
class ProductLine:
    product_quantity: int
    product_price: int
    order_quantity: int

    def total_price(self) -> int:
        return self.product_price * self.order_quantity

    def expected_quantity(self, order_status: str) -> int:
        if order_status == "COMPLETED":
            return self.product_quantity - self.order_quantity
        return self.product_quantity

    def has_enough_stock(self) -> bool:
        return self.order_quantity <= self.product_quantity


@dataclass(frozen=True)
class VerificationCase:
    name: str
    products: list[ProductLine]
    point_amount: int
    expected_status: str

    def total_price(self) -> int:
        return sum(product.total_price() for product in self.products)

    def expected_product_quantities(self) -> list[int]:
        return [
            product.expected_quantity(self.expected_status)
            for product in self.products
        ]

    def expected_point_amount(self) -> int:
        if self.expected_status == "COMPLETED":
            return self.point_amount - self.total_price()
        return self.point_amount

    def basis_lines(self) -> list[str]:
        insufficient_products = [
            (index, product)
            for index, product in enumerate(self.products, start=1)
            if not product.has_enough_stock()
        ]

        if insufficient_products:
            lines = [
                "at least one product has insufficient stock",
            ]
            for index, product in insufficient_products:
                lines.append(
                    f"product[{index}].order_quantity({product.order_quantity}) "
                    f"> product[{index}].stock({product.product_quantity})"
                )
            lines.extend(
                [
                    "all product quantities remain unchanged when reservation fails",
                    "point is not decreased because payment is not requested",
                ]
            )
            return lines

        if self.total_price() > self.point_amount:
            return [
                "all products have enough stock",
                f"total_price({self.total_price()}) > point.amount({self.point_amount})",
                "all product quantities are restored by compensation after payment failure",
                "point remains unchanged because payment failed",
            ]

        return [
            "all products have enough stock",
            f"total_price({self.total_price()}) <= point.amount({self.point_amount})",
            "each product quantity is decreased by its order quantity",
            "point is decreased by total price",
        ]


def verification_cases() -> list[VerificationCase]:
    return [
        VerificationCase(
            name="insufficient_quantity",
            products=[
                ProductLine(
                    product_quantity=1,
                    product_price=10,
                    order_quantity=2,
                )
            ],
            point_amount=100,
            expected_status="FAILED",
        ),
        VerificationCase(
            name="insufficient_point",
            products=[
                ProductLine(
                    product_quantity=1,
                    product_price=10,
                    order_quantity=1,
                )
            ],
            point_amount=9,
            expected_status="FAILED",
        ),
        VerificationCase(
            name="success",
            products=[
                ProductLine(
                    product_quantity=1,
                    product_price=10,
                    order_quantity=1,
                )
            ],
            point_amount=10,
            expected_status="COMPLETED",
        ),
        VerificationCase(
            name="multi_product_success",
            products=[
                ProductLine(
                    product_quantity=3,
                    product_price=10,
                    order_quantity=2,
                ),
                ProductLine(
                    product_quantity=2,
                    product_price=20,
                    order_quantity=1,
                ),
            ],
            point_amount=40,
            expected_status="COMPLETED",
        ),
        VerificationCase(
            name="multi_product_insufficient_quantity",
            products=[
                ProductLine(
                    product_quantity=3,
                    product_price=10,
                    order_quantity=2,
                ),
                ProductLine(
                    product_quantity=1,
                    product_price=20,
                    order_quantity=2,
                ),
            ],
            point_amount=100,
            expected_status="FAILED",
        ),
        VerificationCase(
            name="multi_product_insufficient_point",
            products=[
                ProductLine(
                    product_quantity=3,
                    product_price=10,
                    order_quantity=2,
                ),
                ProductLine(
                    product_quantity=2,
                    product_price=20,
                    order_quantity=1,
                ),
            ],
            point_amount=39,
            expected_status="FAILED",
        ),
    ]
