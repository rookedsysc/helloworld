from fastapi import Depends

from app.service import TestSubService


class TestService:
    def __init__(self, test_sub_service: TestSubService = Depends()):
        self.test_sub_service = test_sub_service

    def helloworld(self) -> str:
        return self.test_sub_service.helloworld()
