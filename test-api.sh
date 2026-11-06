#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=== REGISTER ==="
curl -i -X POST "$BASE_URL/users/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"1234"}'

echo
echo "=== LOGIN ==="

LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/users/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"1234"}')

echo "$LOGIN_RESPONSE"

TOKEN=$(echo "$LOGIN_RESPONSE" | sed -E 's/.*"token":"([^"]+)".*/\1/')

echo
echo "Token: $TOKEN"

echo
echo "=== CREATE MEDIA ==="

curl -i -X POST "$BASE_URL/media" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title":"Interstellar",
    "description":"Science fiction movie",
    "mediaType":"MOVIE",
    "releaseYear":2014,
    "genres":["SCIFI","ADVENTURE"],
    "ageRestriction":12
  }'

echo
echo "=== GET ALL MEDIA ==="

curl -i -X GET "$BASE_URL/media" \
  -H "Authorization: Bearer $TOKEN"

echo
echo "=== GET MEDIA 1 ==="

curl -i -X GET "$BASE_URL/media/1" \
  -H "Authorization: Bearer $TOKEN"

echo
echo "=== UPDATE MEDIA 1 ==="

curl -i -X PUT "$BASE_URL/media/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title":"Interstellar Updated",
    "description":"Updated description",
    "mediaType":"MOVIE",
    "releaseYear":2014,
    "genres":["SCIFI","MYSTERY"],
    "ageRestriction":14
  }'

echo
echo "=== DELETE MEDIA 1 ==="

curl -i -X DELETE "$BASE_URL/media/1" \
  -H "Authorization: Bearer $TOKEN"

echo
echo "=== INVALID TOKEN ==="

curl -i -X GET "$BASE_URL/media" \
  -H "Authorization: Bearer wrong-token"

echo
echo "=== INVALID ID ==="

curl -i -X GET "$BASE_URL/media/abc" \
  -H "Authorization: Bearer $TOKEN"

echo
echo "=== FINISHED ==="