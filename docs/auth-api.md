# Authentication API

Base URL:
http://localhost:8080/api/v1

---

# Login

Endpoint:

POST /auth/login

Controller:
AuthController

Request DTO:
LoginRequest

Example Request:

```json
{
  "email": "user@example.com",
  "password": "password"
}
```

Response:

```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhZG1pbkBqYXBwLmRlIiwiaWF0IjoxNzc4Mjc3NDIzLCJleHAiOjE3NzgzNjM4MjN9.SI2_SwAxVsIPF8NkIWxo6IDbs8eDONs39FuUmBngDhx91TROLPxma6cMppvQOtCj"
}
```