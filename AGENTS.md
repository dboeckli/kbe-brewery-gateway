# AGENTS.md

Spring Boot 4 (parent 4.1.1) / Spring Cloud Gateway (reactive, WebFlux) on **Java 25** (enforced by
the maven-enforcer plugin). Single Maven module, package `ch.dboeckli.springframeworkguru.kbe.sfgrestgateway`.
It is an API gateway that routes to the sibling kbe-brewery services (beer / inventory / order),
backed by MySQL, Artemis (JMS) and the ELK stack (elasticsearch / kibana / filebeat). The Docker
image is published as `domboeckli/kbe-brewery-gateway`, the Helm chart as
`kbe-brewery-gateway-chart`.

## Build & test commands

- Full build: `./mvnw clean verify` — format checks, unit (`*Test`, surefire) + IT (`*IT`, failsafe)
  tests, Helm lint/template. `./mvnw verify` also runs the unit tests.
- Unit tests only: `./mvnw test`. Single test: `./mvnw test -Dtest=GatewayApplicationTest#methodName`.
- `./mvnw clean install` additionally builds the Docker image and packages the Helm chart into
  `target/helm/repo/` (parent `*-chart-<version>.tgz` plus the `-mysql-chart` / `-jms-chart` /
  `-elasticsearch-chart` / `-kibana-chart` / `-filebeat-chart` subchart tgz). Skip the Docker build
  with `-Dskip.docker.build=true`.
- `-Dskip.start.stop.springboot=true` skips the in-build app boot (spring-boot:start/stop) that
  runs during the IT phase.
- Start locally: `./mvnw spring-boot:run` (app on `:9090`).

After changing code, always verify: run the relevant Maven goal above and report its output
(evidence, not just "done").

## Sandbox build quirk (background)

This sandbox mounts the repo via filesystem passthrough, which blocks symlinks — Spotless's
`npm install` (prettier) would fail with `EPERM` unless npm skips bin links. The sandbox kit sets
`npm_config_bin_links=false` globally (`spec.yaml` → `environment.variables`), so no manual export
is needed here. On a normal host (Windows/CI) this does not apply either.

## Formatting is enforced (fails the `validate` phase)

- Java: Spring Java Format → fix with `./mvnw spring-javaformat:apply`.
- Everything else (pom.xml, `**/*.md` except `AGENTS.md`/`CLAUDE.md`, json, application yaml,
  `**/*.sh`): Spotless → fix with `./mvnw spotless:apply`.

## External dependency gotcha

- DTOs and helpers come from the external module `ch.dboeckli...:kbe-brewery-lib` (GitHub Packages,
  `maven.pkg.github.com`). Without a PAT in `~/.m2/settings.xml` (server id `github`) the build
  cannot resolve dependencies.

## Helm specifics

- Chart name `kbe-brewery-gateway-chart`, package version `helm.chart.version` (SemVer-conform,
  lowercase `-snapshot`). `fullnameOverride: kbe-brewery-gateway` in `values.yaml` makes the
  release name deterministic; the aliased local subcharts (mysql, jms, elasticsearch, kibana,
  filebeat) each get a `fullnameOverride` (`kbe-brewery-gateway-<subchart>`). `helm-charts/Chart.yaml`
  names the subcharts with a `-chart` suffix (e.g. `kbe-brewery-gateway-mysql-chart`).
- The four sibling dependencies (`helm-charts/dependencies-Chart.yaml`) are pulled from Docker Hub
  (`oci://registry-1.docker.io/domboeckli`, names `kbe-brewery-*-chart`, lowercase `-snapshot`
  versions). Their service FQDNs in `values.yaml` are `kbe-brewery-gateway-<alias>`.
- Helm packaging/push runs via `exec-maven-plugin` (Helm v4 compatible), not the kokuwa
  helm-maven-plugin.

## Test conventions

- Naming matters: `*Test` = unit (surefire), `*IT` = integration (failsafe). A `*Test` class will
  not run during `verify`'s failsafe phase and vice versa.
- ITs are `@SpringBootTest`; the build boots the app (spring-boot:start/stop) against the Docker
  Compose services defined in `compose.yaml` (mysql, jms, ELK, inventory/beer/order apps) unless
  `-Dskip.start.stop.springboot=true`.

## Architecture

- Reactive API gateway (Spring Cloud Gateway WebFlux); routes in `helm-charts/templates/deployment.yaml`
  (env `SPRING_CLOUD_GATEWAY_SERVER_WEBFLUX_ROUTES_*`) point to the sibling services.
- `compose.yaml` starts the sibling services locally for tests; `docker-manual/compose-local.yaml`
  is a legacy variant.

## Running locally

- `compose.yaml` is auto-started via `spring.docker.compose` on boot (mysql :3306, jms :61616/:8161,
  elasticsearch :9200, kibana :5601, beer :8080, inventory :8082, order :8081, failover :8083).
- Artemis console: http://localhost:8161/console, Kibana: http://localhost:5601
- Manual API testing: IntelliJ HTTP files in `restRequest/`.

## Deploy / CI

- Deployment is Helm-only: chart in `helm-charts/` (name `kbe-brewery-gateway-chart`), packaged to
  `target/helm/repo/`, release name = `kbe-brewery-gateway`, namespace `kbe-brewery-gateway`. A
  `k8s/` source dir no longer exists — deploy via Helm only (see README, `.run/` scripts).
- CI (`.github/workflows/`): `maven-build.yml` builds + deploys snapshots and triggers
  `deploy-and-test-cluster.yml`; `release.yml` runs `mvn release:prepare release:perform` on
  main/master only (version must be `-SNAPSHOT`); SonarCloud analysis runs in the `analyze` job.
- Dependency updates are managed via `.github/dependabot.yml` (actions) and `.github/renovate.json`;
  validate changes with `renovate-config-validator`.
