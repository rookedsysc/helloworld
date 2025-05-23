from fastapi import APIRouter, Depends

from app.service.TestService import TestService

TestRouter = APIRouter(prefix="/v1/tests", tags=["tests"])


@TestRouter.get("/")
def test(test_service: TestService = Depends()):
    return test_service.helloworld()
