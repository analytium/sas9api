# Level 2 – Containers

SAS9API is deployed as **one Spring Boot process** (`sas-proxy.jar`). It also serves a browser-based Swagger UI, reads its configuration from files and environment variables, and writes a log file. All SAS work goes to the external SAS 9.4 servers.

```mermaid
C4Container
    title Container Diagram - SAS9API

    Person(consumer, "API consumer", "Developer, analyst or SAS admin")
    System_Ext(clientApp, "Client application", "BI tool, web app or script")
    Person(operator, "Operator", "Deploys and configures SAS9API")

    System_Boundary(sas9api, "SAS9API") {
        Container(swagger, "Swagger UI", "Springfox, runs in browser", "Interactive API docs at /swagger-ui.html")
        Container(api, "SAS proxy API", "Java 8, Spring Boot 2.3, Tomcat", "sas-proxy.jar: REST endpoints under /sas, JSON or XML")
        ContainerDb(config, "Configuration", "YAML, properties, env vars", "SAS servers, service accounts, API keys, TLS keystore")
        ContainerDb(logs, "Application log", "app.log file", "Request and SAS connection logging")
    }

    Enterprise_Boundary(sasPlatform, "SAS 9.4 platform") {
        System_Ext(mdServer, "SAS Metadata Server", "SAS 9.4 metadata")
        System_Ext(wsServer, "SAS Workspace Server", "SAS 9.4 compute")
        SystemDb_Ext(sasData, "SAS libraries", "SAS data sets")
    }

    Rel(consumer, swagger, "Uses", "Browser")
    Rel(consumer, api, "Calls", "HTTPS")
    Rel(clientApp, api, "Calls", "HTTPS")
    Rel(swagger, api, "Calls", "HTTP(S)")
    Rel(operator, config, "Edits")
    Rel(api, config, "Reads at startup")
    Rel(api, logs, "Writes")
    Rel(api, mdServer, "Metadata queries and updates", "IOM")
    Rel(api, wsServer, "SAS code and SQL", "IOM Bridge")
    Rel(wsServer, sasData, "Reads/writes")

    UpdateRelStyle(consumer, swagger, $offsetY="-35")
    UpdateRelStyle(consumer, api, $offsetY="-35")
    UpdateRelStyle(operator, config, $offsetY="-35")
    UpdateRelStyle(swagger, api, $offsetX="-25", $offsetY="-10")
    UpdateRelStyle(api, config, $offsetX="-45", $offsetY="-15")
    UpdateRelStyle(api, mdServer, $offsetX="-70")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

## Elements

| Container | Technology | Responsibilities |
|---|---|---|
| SAS proxy API | Java 8, Spring Boot 2.3.8, embedded Tomcat, SAS 9.4 client JARs (`jars/`) | Serves the REST API under `/sas` (about 40 endpoints, JSON by default, XML on request). It listens on HTTPS `:8080`, and HTTP on `:8082` redirects to HTTPS. Each request opens its own SAS metadata and/or workspace session and closes it when done. Components: [03a](03a-components-request-handling.md) and [03b](03b-components-sas-connectivity.md). |
| Swagger UI | Springfox (`springfox-swagger2` 3.0.0, `springfox-swagger-ui` 2.10.5), Swagger 2 spec | API docs and a "try it out" console, served by the API process and run in the browser. Requests go to the host in `swagger.host`. |
| Configuration | `config/application.yml`, `src/main/resources/application.properties`, Spring profiles, environment variables | `proxy.servers[]` (Metadata Server host, port and users with password and API key), `proxy.apikeyEnabled` / `basicAuthEnabled`, `proxy.default_workspace_server_name`, `datasetdata.*` fetch limits, and `server.ssl.*` (PKCS12 keystore bundled on the classpath). |
| Application log | Spring Boot file logging (`logging.file.name: app.log`) | DEBUG logging for `com.sas` and `com.codexsoft.sas` by default. |

## Notes

* **Stateless by design, apart from one exception.** No database is used. Basic-auth credentials are, however, written to a shared singleton. See [Risks](04-risks.md#r2-basic-auth-credentials-leak-across-requests).
* **Two hops to reach data.** Data endpoints first ask the Metadata Server where the Workspace Server is, then open an IOM Bridge session to it. SQL runs over that session's data service using SAS's `MVAConnection` JDBC driver. No separate database server is involved.
* **Build:** `.github/workflows/build.yml` installs the SAS JARs into the local Maven repository and runs `mvn package`, producing the `sas-proxy.jar` artifact.
