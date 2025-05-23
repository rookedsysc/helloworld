from app.controller.TestRouter import TestRouter
from fastapi import FastAPI


app = FastAPI()

app.include_router(TestRouter)


@app.get("/")
async def root():
    return {"message": "Hello World"}
