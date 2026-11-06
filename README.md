# Media Ratings Platform

The Media Ratings Platform is a Java REST backend application for managing users and media entries.

The application uses Java's built-in HTTP server and PostgreSQL for persistent data storage. PostgreSQL runs inside a Docker container.

## Technologies

- Java
- Maven
- PostgreSQL
- Docker
- Jackson
- JDBC
- Java HttpServer

## Requirements

Before running the project, make sure the following tools are installed:

- Java
- Maven
- Docker

Check the installations with:

```bash
java --version
mvn --version
docker --version
```

## Database Setup

The PostgreSQL database runs inside Docker.

Create and start the PostgreSQL container with:

```bash
docker run -d \
  --name swen-db \
  -p 5432:5432 \
  -e POSTGRES_PASSWORD=mysecretpassword \
  postgres:16
```

If the container already exists, start it with:

```bash
docker start swen-db
```

The application uses the following database configuration:

```text
Host: localhost
Port: 5432
Database: postgres
User: postgres
Password: mysecretpassword
```

## Database Initialization

The file `seed.sql` creates the required database tables and inserts example data.

Run the seed file with:

```bash
psql -h localhost -U postgres -d postgres -f seed.sql
```

When asked for the PostgreSQL password, use:

```text
mysecretpassword
```

The database contains the following main tables:

- `users`
- `media`
- `media_genres`

## Build

Build the project with Maven:

```bash
mvn clean package
```

A successful build should end with:

```text
BUILD SUCCESS
```

## Start the Server

Run `Main.java` from IntelliJ.

The REST server starts on:

```text
http://localhost:8080
```

The console should display:

```text
server started at :8080
```

## Authentication

Media endpoints require authentication.

After a successful login, the server returns a token:

```json
{
  "token": "example-token"
}
```

The token must be included in the `Authorization` header:

```text
Authorization: Bearer <token>
```

## API Endpoints

### Register User

```http
POST /users/register
```

Example request:

```json
{
  "username": "testuser",
  "password": "1234"
}
```

Possible responses:

```text
201 Created
409 Conflict
```

### Login User

```http
POST /users/login
```

Example request:

```json
{
  "username": "testuser",
  "password": "1234"
}
```

Possible responses:

```text
200 OK
401 Unauthorized
```

### Get All Media

```http
GET /media
```

Requires a valid Bearer token.

Successful response:

```text
200 OK
```

### Get Media by ID

```http
GET /media/{id}
```

Possible responses:

```text
200 OK
400 Bad Request
404 Not Found
401 Unauthorized
```

### Create Media

```http
POST /media
```

Example request:

```json
{
  "title": "Interstellar",
  "description": "Science fiction movie",
  "mediaType": "MOVIE",
  "releaseYear": 2014,
  "genres": [
    "SCIFI",
    "ADVENTURE"
  ],
  "ageRestriction": 12
}
```

The `creatorId` is automatically determined from the authenticated user token.

Successful response:

```text
201 Created
```

### Update Media

```http
PUT /media/{id}
```

Only the creator of the media entry is allowed to update it.

Possible responses:

```text
200 OK
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
```

### Delete Media

```http
DELETE /media/{id}
```

Only the creator of the media entry is allowed to delete it.

Possible responses:

```text
204 No Content
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
```

## Media Types

Supported media types are:

```text
MOVIE
SERIES
GAME
```

## Genres

Supported genres are:

```text
ACTION
ADVENTURE
COMEDY
FANTASY
HORROR
MYSTERY
ROMANCE
SCIFI
```

A media entry can contain multiple genres.

Genres are stored in the separate `media_genres` table.

## API Test Script

The project contains the file:

```text
test-api.sh
```

Make the script executable:

```bash
chmod +x test-api.sh
```

Run it with:

```bash
./test-api.sh
```

The script tests the REST API using curl requests.

It includes tests for:

- user registration
- user login
- token authentication
- media creation
- reading media
- updating media
- deleting media
- invalid tokens
- invalid media IDs

The server must be running before executing the script.

## Architecture

The application is separated into several layers.

```text
HTTP Server
    |
    v
Repositories
    |
    v
UnitOfWork
    |
    v
DatabaseManager
    |
    v
PostgreSQL
```

Repositories are used to separate database access from the HTTP server logic.

Prepared statements are used for SQL queries to prevent SQL injection.

Transactions are handled using the `UnitOfWork` class.

## Project Structure

```text
mrp/
├── src/
│   └── main/
│       └── java/
│           └── at/
│               └── technikum/
│                   ├── model/
│                   ├── repository/
│                   ├── server/
│                   └── services/
├── pom.xml
├── seed.sql
├── test-api.sh
└── README.md
```

## Tested Functionality

The following functionality has been manually tested:

```text
POST   /users/register       -> 201 Created
POST   /users/register       -> 409 Conflict for duplicate username

POST   /users/login          -> 200 OK
POST   /users/login          -> 401 Unauthorized for invalid credentials

POST   /media                -> 201 Created
GET    /media                -> 200 OK
GET    /media/{id}           -> 200 OK
GET    /media/{id}           -> 404 Not Found for unknown ID
PUT    /media/{id}           -> 200 OK
DELETE /media/{id}           -> 204 No Content

GET    /media                -> 401 Unauthorized without token
GET    /media                -> 401 Unauthorized with invalid token
GET    /media/abc            -> 400 Bad Request
```

Media genres were also tested for create, read and update operations.

## GitHub

Repository:

https://github.com/KingAlina/mrp