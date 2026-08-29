# Codeforces

A Quarkus service that pulls user profiles and blog entries from the
[Codeforces API](https://codeforces.com/apiHelp), fans them out through Kafka, and
persists them to MongoDB while indexing them in OpenSearch for full-text search.

## How it works

```
GET /user-info/{handles}          Codeforces API
GET /user-blogs/{handle}     ->        |
                                       v
                              Kafka (codeforcesUserInfo / codeforcesBlogEntry)
                                       |
                                       v
                              kafkaConsumer  ->  MongoDB  (source of truth)
                                       |      ->  OpenSearch (search index)
                                       v
                              Redis (per-partition processed offsets)

GET /searchOnUserInfo/{query}     queries the OpenSearch indexes
GET /searchOnBlogEntry/{query}
```

Records already present in MongoDB are skipped rather than re-published, so repeated
fetches for the same handle are cheap.

Consumer offsets are tracked in Redis by `kafkaConsumer` and replayed by
`customRebalanceListener` on partition assignment, so the consumers resume from the
last record they actually finished processing rather than from Kafka's committed offsets.

## Configuration

Credentials and service addresses come from the environment. Copy the template and fill
it in:

```shell script
cp .env.example .env
```

`CODEFORCES_API_KEY` and `CODEFORCES_API_SECRET` are required — generate them at
<https://codeforces.com/settings/api>. Never commit the filled-in `.env`.

The defaults in `application.properties` point at `localhost`, which is what you want for
a local run; `docker-compose.yml` overrides them with the in-network hostnames.

## Running with Docker Compose

Brings up the app plus Kafka, Zookeeper, MongoDB, Redis and OpenSearch:

```shell script
./gradlew build
docker compose up --build
```

The app is served on <http://localhost:8080>. Compose waits for each backing service to
pass its health check before starting the app.

## Running locally

Start the backing services and run the app against them on the host:

```shell script
docker compose up -d mongodb redis opensearch kafka zookeeper
./gradlew quarkusDev
```

Kafka advertises two listeners: `kafka:9092` inside the compose network, and
`localhost:29092` for clients on the host. `.env.example` already points at the latter,
which is what a local run needs — the in-network address is not resolvable from the host.

Dev mode serves the app on <http://localhost:3000> with the Dev UI at
<http://localhost:3000/q/dev/>.

## Endpoints

| Method | Path                          | Description                                          |
| ------ | ----------------------------- | ---------------------------------------------------- |
| GET    | `/user-info/{handles}`        | Fetch profiles (semicolon-separated handles)         |
| GET    | `/user-blogs/{handle}`        | Fetch a user's blog entries                          |
| GET    | `/searchOnUserInfo/{query}`   | Full-text search over indexed profiles               |
| GET    | `/searchOnBlogEntry/{query}`  | Full-text search over indexed blog entries           |
| GET    | `/q/health`                   | Liveness / readiness                                 |

## Tests

```shell script
./gradlew test
```

## Packaging

```shell script
./gradlew build
java -jar build/quarkus-app/quarkus-run.jar
```

For an über-jar or a native executable, see the
[Quarkus Gradle tooling guide](https://quarkus.io/guides/gradle-tooling).
