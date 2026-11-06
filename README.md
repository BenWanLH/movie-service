# movie-service

Kotlin Spring Boot service that owns **movies, credits and artwork**.

- **GraphQL subgraph** (`/graphql`): movie queries and mutations, plus adding and removing credits. It's one of the two subgraphs that the Apollo Router combines into the API used by `movie-web`.
- **REST artwork endpoints**: `POST /movies/{id}/artwork` (multipart, field `file`; JPEG/PNG/WebP, max 5 MB) and `GET /movies/{id}/artwork`
- **gRPC server** (`MovieCreditsService`): `DeleteCreditsByPerson`, called by `people-service` when a person is deleted
- **gRPC client** of `people-service`: `GetPerson`, to check that a person exists and has the role before a credit is added

The gRPC contracts are in the `protos` submodule (at `protos/`).

A **credit** links a person to a movie with a role and an optional character name. movie-service stores only the person's id. In its schema, `Credit.person` is a `Person` reference with just an `id` (`@key(fields: "id", resolvable: false)`), and the router fetches the rest of the person from `people-service`.

Movies and credits are stored in an H2 file database under `./data/`, with the schema managed by Flyway. Uploaded artwork is saved to `./data/artwork/`.

## Prerequisites

- **JDK 21** (pinned in `.tool-versions`; with [asdf](https://asdf-vm.com/): `asdf install`)
- **people-service** running, for adding credits (see its README)

Gradle comes with the project (`./gradlew`), so it doesn't need installing.

## Clone

```bash
git clone --recurse-submodules https://github.com/BenWanLH/movie-service.git
```

If you already cloned without submodules, run `git submodule update --init`. Without it, `protos/` is empty and the build fails.

## Run

```bash
./gradlew bootRun
```

| Port | What |
|---|---|
| `8080` | GraphQL (`POST /graphql`), GraphiQL (http://localhost:8080/graphiql), REST artwork |
| `9091` | gRPC server |

GraphiQL here only shows movie-service's subgraph, so `Person` has only an `id`. Query through the router (http://localhost:4000/graphql) to get people's names.

Example query, through the router:

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

Explore the gRPC server with [grpcurl](https://github.com/fullstorydev/grpcurl):

```bash
grpcurl -plaintext localhost:9091 list
```

Movie queries and mutations work even if people-service is down. Adding a credit returns a GraphQL error instead.

To start with empty data, stop the service and delete the `data/` folder.

## Tests

```bash
./gradlew test
```

Tests run with the `test` profile (set in `build.gradle.kts`). It uses an in-memory H2 database, a random gRPC server port, and writes artwork to `build/test-artwork`, so the tests don't touch `./data/` and can run while the service is running. **people-service doesn't need to be running**, because the gRPC client is mocked.

The HTML report is written to `build/reports/tests/test/index.html`.
