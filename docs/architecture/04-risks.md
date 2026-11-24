# Architecture and security risks

Risks found while reading the code to draw the C4 views. Each one names the files involved and a suggested fix. Severity is a first estimate for triage, not the result of a formal assessment. Secret values are deliberately **not** repeated here.

| # | Risk | Severity |
|---|---|---|
| R1 | [Secrets and credentials committed to the repository](#r1-secrets-and-credentials-committed-to-the-repository) | High |
| R2 | [Basic-auth credentials leak across requests](#r2-basic-auth-credentials-leak-across-requests) | High |
| R3 | [Authentication is off by default and callers choose the service account](#r3-authentication-is-off-by-default-and-callers-choose-the-service-account) | High |
| R4 | [Caller-supplied Workspace Server host receives SAS credentials](#r4-caller-supplied-workspace-server-host-receives-sas-credentials) | High |
| R5 | [Arbitrary SAS code execution through `/sas/cmd`](#r5-arbitrary-sas-code-execution-through-sascmd) | Medium (by design) |
| R6 | [SQL built by string formatting](#r6-sql-built-by-string-formatting) | Medium |
| R7 | [Swagger host hard-codes credentials and a public IP](#r7-swagger-host-hard-codes-credentials-and-a-public-ip) | Medium |
| R8 | [Self-signed keystore bundled in the JAR](#r8-self-signed-keystore-bundled-in-the-jar) | Medium |
| R9 | [Outdated platform and libraries](#r9-outdated-platform-and-libraries) | Medium |
| R10 | [Stale Actuator shutdown setting](#r10-stale-actuator-shutdown-setting) | Low |
| R11 | [Dead licensing and anti-debug code](#r11-dead-licensing-and-anti-debug-code) | Low |

### R1. Secrets and credentials committed to the repository

* `keys/private.key` (repo root), `keys/public_key.der` and `src/main/resources/keys/public_key.der` are committed. `application.properties` also points at a `private_key.der` path.
* `config/application.yml` holds **plaintext passwords and API keys** for SAS accounts (including admin-type accounts) on two Metadata Servers, one of them named `prod`. It also holds the TLS keystore password.
* `src/main/resources/localhost-ssl-keystore.p12` and `license/L*_license.dat` are committed.

**Fix:** treat all of these as compromised. Rotate the SAS passwords and API keys and replace the key pair. Move secrets to environment variables or a secret store and keep only placeholders in Git. Consider purging them from history (`git filter-repo`), and add secret scanning to CI.

### R2. Basic-auth credentials leak across requests

When Basic auth succeeds, `AuthRequestsInterceptor#overrideConnectionProperties` calls `ProxyConfigModel#setConnection`. `ProxyConfigModel` is a **singleton**, so from then on every request, from any caller, runs with the last Basic-auth user's SAS credentials. That includes requests authenticated by API key, and requests sent while auth is off. Concurrent users can also overwrite each other's credentials mid-request.

**Fix:** keep the authenticated principal in request scope (a request attribute or Spring Security's `SecurityContext`) and never write it to shared configuration.

### R3. Authentication is off by default and callers choose the service account

`proxy.apikeyEnabled` and `proxy.basicAuthEnabled` both default to `false`, and the interceptor then lets every request through. The `serverName` and `userName` query parameters let a caller choose **which configured account** to act as, admin accounts included. Even with API keys on, the key is checked against the account named by `userName`, so the key, not the caller, decides the privilege level.

**Fix:** default to auth on. Bind each API key to one account on the server side, and don't let unauthenticated callers choose credentials.

### R4. Caller-supplied Workspace Server host receives SAS credentials

`/sas/cmd` and the "direct connection" library and data-set endpoints accept `serverUrl` / `serverPort`. `ConnectionHelpers#getWorkspaceConnectionPropsByHost` then opens an IOM Bridge connection **to that host using the configured service-account credentials**. Any caller can point the proxy at a host they control and capture the login attempt. It also allows server-side request forgery (SSRF) to internal hosts.

**Fix:** remove these parameters, or check them against an allow-list built from metadata.

### R5. Arbitrary SAS code execution through `/sas/cmd`

`PUT /sas/cmd` submits any SAS code to a Workspace Server as the resolved account. This is a feature, but together with R2–R4 it gives remote code execution to anyone who can reach the port. If `XCMD` is enabled on the Workspace Server, that extends to OS commands.

**Fix:** put it behind a separate role or feature flag, log every submission, and document that it should be off in production unless needed.

### R6. SQL built by string formatting

`DataSetDataDao` (and the other JDBC DAOs) insert `libraryName`, `datasetName` and `byKey` into SQL with `String.format` (`SELECT * FROM %s.%s …`, `DELETE FROM %s.%s …`). Filter values go through `JDBCUtils.quoteValue`, but the identifiers come straight from path variables.

**Fix:** validate identifiers (SAS name rules or a quoted `'name'n` literal), and use bind parameters wherever values are involved.

### R7. Swagger host hard-codes credentials and a public IP

`swagger.host` in `config/application.yml` contains a `user:password@` prefix and a public IP address. Swagger UI is also set to `protocols("http")`, even though the server forces HTTPS.

**Fix:** set the host per environment with no credentials in it, and switch to `https`.

### R8. Self-signed keystore bundled in the JAR

`server.ssl.key-store: classpath:localhost-ssl-keystore.p12` holds a self-signed `localhost` certificate, and its password is in the YAML. Unless overridden, every deployment ships the same key.

**Fix:** load the keystore from an external path given at deploy time, or terminate TLS at a reverse proxy.

### R9. Outdated platform and libraries

Spring Boot 2.3.8 and Java 8 are past open-source support. `log4j:log4j:1.2.17` is declared and has known CVEs (e.g. CVE-2019-17571, CVE-2021-4104), and Springfox is unmaintained.

**Fix:** check whether log4j 1.x is still needed alongside `log4j-over-slf4j` and remove it if not. Plan an upgrade path, keeping in mind the Java versions the SAS 9.4 client JARs support.

### R10. Stale Actuator shutdown setting

`spring-boot-starter-actuator` is on the classpath, and `application.yml` sets `endpoints.shutdown.enabled: true, sensitive: false`. That is Spring Boot **1.x** syntax, ignored by 2.3, so the endpoint is not currently exposed. It shows intent that could be "fixed" into an unauthenticated shutdown endpoint.

**Fix:** remove the setting, or move it to `management.endpoint.*` with exposure limited and secured.

### R11. Dead licensing and anti-debug code

`CheckDebug` (pointcut commented out, would call `System.exit`), `DateChecker` and `SiteNumberChecker` (unused), `LicenseCondition` (always `true`) and the `/res1`–`/res3` placeholder controllers that return `null` all ship in the build. They add confusion and attack surface.

**Fix:** delete them, or finish the feature behind a clear flag.
