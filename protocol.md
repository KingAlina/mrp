# Media Ratings Platform – Protocol

## 1. Project Overview

The goal of the intermediate version of the Media Ratings Platform (MRP) was to implement a standalone REST backend in Java.

The application provides user registration and login, token-based authentication, and CRUD operations for media entries. Data is persisted in a PostgreSQL database running inside Docker.

The implementation does not use a full-stack web framework such as Spring. HTTP communication is handled using Java's built-in `HttpServer`.

The current intermediate version supports:

- User registration
- User login
- Bearer token authentication
- Creation of media entries
- Reading all media entries
- Reading a single media entry
- Updating media entries
- Deleting media entries
- Ownership checks for update and delete operations
- PostgreSQL persistence
- Multiple genres per media entry
- Database initialization using `seed.sql`
- API testing using `test-api.sh`

---

## 2. Technical Steps

### 2.1 Initial Project Setup

The project was created as a Maven-based Java application.

The main dependencies are:

- Jackson for JSON serialization and deserialization
- PostgreSQL JDBC driver for database access

The application is built using:

```bash
mvn clean package
```

The final intermediate version successfully builds with Maven and produces a JAR file.

---

### 2.2 PostgreSQL and Docker Setup

PostgreSQL 16 is used as the database.

The database runs inside a Docker container named:

```text
swen-db
```

The database is accessible on:

```text
localhost:5432
```

The Java application connects using JDBC.

The database connection is centralized inside the `DatabaseManager` class.

This prevents connection configuration from being duplicated throughout the repository classes.

---

### 2.3 Database Schema

The intermediate version uses three main tables:

```text
users
media
media_genres
```

The `users` table stores:

- id
- username
- password

The username is unique.

The `media` table stores:

- id
- creator_id
- title
- description
- media_type
- release_year
- age_restriction

Genres are stored separately in `media_genres`.

This table contains:

- media_id
- genre

A separate table was chosen because a single media entry can have multiple genres.

The combination of `media_id` and `genre` is used as the primary key.

A foreign key connects `media_genres.media_id` to `media.id`.

The relationship uses:

```sql
ON DELETE CASCADE
```

Therefore, deleting a media entry automatically deletes all genres belonging to it.

---

### 2.4 Database Seed File

A `seed.sql` file was added to make the database setup reproducible.

It:

1. removes the existing project tables,
2. recreates the required tables,
3. inserts sample users,
4. inserts sample media entries,
5. inserts sample genres.

The database can therefore be reset into a known state before testing.

Example:

```bash
psql -h localhost -U postgres -d postgres -f seed.sql
```

This also makes it easier for another developer or the lecturer to test the project without manually creating database tables.

---

## 3. Architecture Decisions

### 3.1 Layer Separation

The application is separated into multiple responsibilities:

```text
HTTP Server
     |
     v
Repository Layer
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

The HTTP server is responsible for:

- receiving HTTP requests,
- checking HTTP methods and paths,
- authentication,
- reading JSON request bodies,
- returning HTTP responses.

The repositories are responsible for:

- SQL statements,
- mapping database rows to Java objects,
- creating, reading, updating and deleting data.

Database connection management is kept outside the HTTP server.

This separation was chosen to avoid mixing HTTP logic and SQL logic in the same part of the application.

---

### 3.2 Repository Pattern

A generic repository interface is used:

```text
Repository<T, ID>
```

It contains common CRUD operations:

```text
create
readById
readAll
updateById
deleteById
```

`UserRepository` and `MediaRepository` use this common interface.

The advantage of this design is that CRUD operations follow the same structure for different entities.

Database-specific implementations are handled by:

```text
JdbcUserRepository
JdbcMediaRepository
```

---

### 3.3 UnitOfWork

A `UnitOfWork` class is used to manage database transactions.

Each database operation obtains its own connection through the `UnitOfWork`.

Auto-commit is disabled.

Successful write operations call:

```text
commitTransaction()
```

Operations that do not successfully produce the expected result can call:

```text
rollbackTransaction()
```

The class implements `AutoCloseable`, which makes it possible to use Java's try-with-resources syntax.

This ensures that database connections are closed after an operation.

---

### 3.4 Prepared Statements

SQL queries use `PreparedStatement`.

For example, user input is passed into SQL using placeholders such as:

```sql
SELECT * FROM users WHERE username = ?
```

instead of concatenating strings into the SQL command.

This decision improves the structure of the database code and protects against SQL injection attacks.

---

### 3.5 JSON Handling

Jackson is used to convert between Java objects and JSON.

For example:

```text
JSON request
    ↓
Media object
```

and:

```text
Media object
    ↓
JSON response
```

`User` and `Media` contain no-argument constructors and setters so that Jackson can deserialize incoming JSON directly into model objects.

Separate DTO classes were not used in the final intermediate implementation.

---

## 4. Authentication Design

Users register using:

```text
POST /users/register
```

Duplicate usernames return:

```text
409 Conflict
```

Users log in using:

```text
POST /users/login
```

Invalid credentials return:

```text
401 Unauthorized
```

After a successful login, a UUID token is generated.

The token is associated with the user ID using an in-memory map.

Authenticated requests use:

```text
Authorization: Bearer <token>
```

A missing or unknown token returns:

```text
401 Unauthorized
```

The current token implementation is intentionally simple for the intermediate version.

Tokens are stored only in memory and are therefore lost when the server is restarted.

---

## 5. Media Ownership

A media entry belongs to the user who created it.

The client does not determine the `creatorId`.

Instead, when a media entry is created, the server obtains the current user's ID from the authentication token and sets:

```text
creatorId = authenticated user ID
```

This prevents a user from creating a media entry while pretending to be another user.

For update and delete requests, the server compares the authenticated user with the media entry's creator.

If another user attempts to change or delete the entry, the server returns:

```text
403 Forbidden
```

---

## 6. Genre Persistence

The Java `Media` model contains:

```text
List<Genre>
```

Initially, the media table stored the main media information but did not persist the genre list.

A separate `media_genres` table was introduced to solve this.

### Create

When creating a media entry:

1. the media row is inserted,
2. PostgreSQL returns the generated media ID,
3. every genre is inserted into `media_genres`,
4. the transaction is committed.

### Read

When media is read from the database, a helper method loads its genres using:

```sql
SELECT genre
FROM media_genres
WHERE media_id = ?
```

The resulting strings are converted back into the Java `Genre` enum.

### Update

When media is updated:

1. the main media row is updated,
2. existing genre rows are deleted,
3. the new genres are inserted,
4. the transaction is committed.

### Delete

Genre deletion does not require additional Java code because the database foreign key uses `ON DELETE CASCADE`.

---

## 7. REST Routes and HTTP Status Codes

The following routes were implemented:

```text
POST   /users/register
POST   /users/login

GET    /media
GET    /media/{id}
POST   /media
PUT    /media/{id}
DELETE /media/{id}
```

Meaningful HTTP status codes are returned.

Examples:

```text
200 OK
201 Created
204 No Content
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
405 Method Not Allowed
409 Conflict
```

Examples of tested behavior:

```text
Duplicate username          -> 409 Conflict
Wrong password              -> 401 Unauthorized
Missing token               -> 401 Unauthorized
Invalid token               -> 401 Unauthorized
Invalid ID format           -> 400 Bad Request
Unknown media ID            -> 404 Not Found
Unauthorized modification   -> 403 Forbidden
Successful creation         -> 201 Created
Successful update           -> 200 OK
Successful deletion         -> 204 No Content
```

---

## 8. Test Coverage

### 8.1 Automated Unit Tests

The intermediate implementation currently does not contain a dedicated JUnit unit test suite.

The main focus during this intermediate stage was functional and integration testing of the complete REST flow using curl together with a real PostgreSQL database.

Automated unit tests should be extended in the final version, especially for repository behavior and business logic that will be introduced with ratings, favorites, filtering and recommendations.

---

### 8.2 Functional and Integration Testing

The project contains:

```text
test-api.sh
```

The script automatically executes a sequence of HTTP requests against the running server.

It performs a login, extracts the generated token, creates a media entry and extracts the generated media ID.

Using the dynamically generated ID was important because database IDs may change after seeding or repeated test executions.

The script tests:

- registration,
- login,
- authentication token handling,
- media creation,
- reading all media,
- reading the newly created media,
- updating the newly created media,
- deleting the newly created media,
- invalid authentication,
- invalid media IDs.

The project was also repeatedly built using:

```bash
mvn clean package
```

and the final intermediate build completed successfully.

---

### 8.3 Why These Cases Were Tested

Authentication was tested because all media operations depend on the validity of the Bearer token.

Both successful and unsuccessful login behavior were tested because invalid credentials must not generate an authentication token.

Duplicate registration was tested because the username has to remain unique.

Media creation was tested together with genres to verify both the main `media` table and the related `media_genres` table.

Reading media after creation was tested to verify that persisted data can be reconstructed correctly as Java objects and returned as JSON.

Updating media was tested with changed genres because genre updates require multiple database operations.

Deleting media was tested to verify both the media deletion and the database cascade behavior for genres.

An invalid token was tested to confirm that protected resources cannot be accessed without valid authentication.

An invalid route parameter such as:

```text
/media/abc
```

was tested to verify that an invalid numeric ID returns:

```text
400 Bad Request
```

A nonexistent numeric ID was tested separately because it represents a valid ID format but an unknown resource and should therefore return:

```text
404 Not Found
```

Ownership checks were also verified. A user who does not own a media entry receives:

```text
403 Forbidden
```

when attempting to update or delete it.

---

## 9. Problems Encountered and Solutions

### 9.1 Maven Was Not Available

During development, the command:

```bash
mvn clean package
```

initially failed because Maven was not installed on one development machine.

Maven was installed using Homebrew.

After installation:

```bash
mvn --version
```

worked correctly and the project could be built successfully.

---

### 9.2 PostgreSQL Command-Line Client Was Missing

The PostgreSQL server was running correctly in Docker, but the local `psql` command was initially unavailable.

The PostgreSQL command-line tools were installed using Homebrew.

This made it possible to execute the database initialization directly using:

```bash
psql -h localhost -U postgres -d postgres -f seed.sql
```

---

### 9.3 Media Routing

An early version of the HTTP routing checked only whether the HTTP method was `GET`.

As a result, requests to:

```text
/media/{id}
```

could incorrectly enter the handler for:

```text
/media
```

The conditions were changed so that both the HTTP method and path are checked.

For example:

```text
GET + /media
GET + /media/{id}
```

are now handled separately.

---

### 9.4 Invalid Media IDs

Media IDs are received through the URL as strings.

Calling `Integer.parseInt()` directly can fail when a request contains an invalid value such as:

```text
/media/abc
```

This was solved using `try/catch` around the conversion.

Invalid numeric formats now return:

```text
400 Bad Request
```

---

### 9.5 Genre Data Was Lost

Initially, the `Media` Java class contained a list of genres, but the PostgreSQL media table had no place to persist multiple genres.

The first implementation therefore returned an empty genre list when media was loaded.

This was solved by introducing:

```text
media_genres
```

and adding genre persistence to create, read and update operations.

---

### 9.6 Test Script Used a Fixed Media ID

The first version of `test-api.sh` used:

```text
/media/1
```

for GET, PUT and DELETE.

After introducing seed data, media ID `1` belonged to one of the seeded users.

The test user therefore received:

```text
403 Forbidden
```

when attempting to update or delete it.

This showed that the ownership check worked correctly, but the test script itself was not fully reusable.

The script was changed to read the ID returned by the create request:

```text
MEDIA_ID
```

All subsequent requests now use the dynamically created media ID.

This makes the test independent of existing seed data.

---

### 9.7 Creator ID Security

An earlier approach allowed the request body to contain a creator ID.

This could allow a client to create data for another user.

The server was changed so that `creatorId` is always derived from the authenticated token.

The same user ID is also used for ownership checks during update and delete operations.

---

## 10. Estimated Time Tracking

The following values are estimates because development was completed incrementally over multiple sessions.

| Project Part | Estimated Time |
|---|---:|
| Understanding the specification and initial project setup | 1.5 h |
| Java models and project structure | 1.5 h |
| PostgreSQL and Docker setup | 2 h |
| JDBC connection, DatabaseManager and UnitOfWork | 2 h |
| Repository pattern and user repository | 2.5 h |
| HTTP server and routing | 2.5 h |
| Registration and login | 1.5 h |
| Token authentication and authorization | 1.5 h |
| Media repository and CRUD | 3 h |
| Genre database design and persistence | 2 h |
| Debugging and fixing routing/CRUD issues | 2 h |
| Database seed file | 0.5 h |
| curl test script and manual API testing | 2 h |
| Maven/build/environment setup | 1 h |
| README and project documentation | 1.5 h |
| Protocol | 1 h |
| **Estimated total** | **27 h** |

The values are approximate and are intended to document the relative effort spent on the main parts of the project.

---

## 11. Git History

Git and GitHub were used throughout development.

The repository contains the development history with individual commits for the implemented steps and fixes.

Because the Git history is considered part of the project documentation, individual commits are not duplicated in this protocol.

The repository is available at:

```text
https://github.com/KingAlina/mrp
```

---

## 12. Current Intermediate Status

The intermediate version successfully provides the required technical foundation:

- Java REST server
- HTTP routing
- JSON request and response handling
- PostgreSQL persistence
- PostgreSQL running in Docker
- Repository pattern
- UnitOfWork transaction handling
- Prepared statements
- User registration
- User login
- Bearer token authentication
- Media CRUD
- Ownership authorization
- Multiple genres per media
- Database seed script
- curl API test script
- Maven build
- Git history
- README documentation

The next development stage can build on this architecture to implement the remaining final-version functionality such as ratings, favorites, filtering, recommendations and leaderboard features.