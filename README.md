# movie-service

Kotlin Spring Boot service that owns **movies, credits and artwork**, and is the **single backend entry point** for `movie-web`.

- **GraphQL API** (`/graphql`): movie and person queries and mutations, used by the web app
- **REST artwork endpoints**: `POST /movies/{id}/artwork` (multipart, field `file`; JPEG/PNG/WebP, max 5 MB) and `GET /movies/{id}/artwork`
- **gRPC client** of `people-service`: person queries and mutations are forwarded to it, using the contract in the `protos` submodule (at `protos/`)

A **credit** links a person to a movie with a role and an optional character name. Credits are stored here, and the person behind each credit is resolved from people-service with a single `BatchGetPeople` call per request (`@BatchMapping`), which avoids N+1 calls. Deleting a person through this service also removes their credits.

Movies and credits are stored in an H2 file database under `./data/`, with the schema managed by Flyway. Uploaded artwork is saved to `./data/artwork/`.

## Prerequisites

- **JDK 21** (pinned in `.tool-versions`; with [asdf](https://asdf-vm.com/): `asdf install`)
- **people-service** running on `localhost:9090` (see its README)

Gradle comes with the project (`./gradlew`), so it doesn't need installing.

## Clone

```bash
git clone --recurse-submodules https://github.com/BenWanLH/movie-service.git
```

If you already cloned without submodules, run `git submodule update --init`. Without it, `protos/` is empty and the build fails.

## Run

Start people-service first, then:

```bash
./gradlew bootRun
```

The service listens on **`http://localhost:8080`**.

- GraphiQL (in-browser GraphQL explorer): http://localhost:8080/graphiql
- GraphQL endpoint: `POST http://localhost:8080/graphql`

Example query:

```graphql
query {
  movies(title: "matrix") {
    id
    title
    artworkUrl
    credits { role characterName person { name } }
  }
}
```

Movie queries and mutations work even if people-service is down. Anything that needs people (person queries, adding credits, resolving `Credit.person`) returns a GraphQL error instead.

To start with empty data, stop the service and delete the `data/` folder.

## Tests

```bash
./gradlew test
```

Tests run with the `test` profile (set in `build.gradle.kts`). It uses an in-memory H2 database and writes artwork to `build/test-artwork`, so the tests don't touch `./data/`. **people-service doesn't need to be running**, because the gRPC client is mocked.

The HTML report is written to `build/reports/tests/test/index.html`.
