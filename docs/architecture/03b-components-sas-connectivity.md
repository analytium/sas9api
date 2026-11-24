# Level 3 – Components: SAS connectivity

The part of the **SAS proxy API** container that talks to SAS. There are two paths:

* **Metadata path:** DAOs that use the SAS Java metadata API (JOMA) over an `IOMConnection`.
* **Data and compute path:** an IOM Bridge `WorkspaceConnection`, with an `MVAConnection` JDBC driver on top for SQL.

`SASRestController` and `ConnectionHelpers` are the same components as in [03a](03a-components-request-handling.md). `ConnectionHelpers` is drawn inside this boundary because it lives in the `connections` package and creates connections. `SearchDao` and `ObjectOperationsDao` share one box to keep the diagram readable.

```mermaid
C4Component
    title Component Diagram - SAS proxy API: SAS connectivity

    Person(consumer, "API consumer / client app", "Calls /sas/** over HTTPS")
    Component(ctrl, "SASRestController", "Spring RestController", "REST endpoints under /sas, see 03a")

    Container_Boundary(conn, "SAS proxy API - SAS connectivity") {
        Component(searchOps, "Search and object operations", "Java, SAS JOMA", "SearchDao, ObjectOperationsDao")
        Component(metaDaos, "Metadata DAOs", "Java, SAS JOMA", "Repositories, servers, libraries, persons, groups, permissions, trees")
        Component(helpers, "ConnectionHelpers", "Spring bean", "Workspace Server lookup, see 03a")
        Component(dataDaos, "Data DAOs", "Java, JDBC", "Libraries, data sets, columns, rows")
        Component(parsers, "Metadata parsers", "Java, unxml", "Map metadata XML to models")
        Component(iomConn, "IOMConnection", "SAS JOMA MdFactory", "Metadata session per request")
        Component(wsConn, "WorkspaceConnection", "SAS IOM Bridge", "Workspace session, submits SAS code")
        Component(jdbcConn, "JDBCConnection", "SAS MVAConnection", "JDBC over workspace IDataService")
    }

    Enterprise_Boundary(sasPlatform, "SAS 9.4 platform") {
        System_Ext(mdServer, "SAS Metadata Server", "SAS 9.4 metadata")
        System_Ext(wsServer, "SAS Workspace Server", "SAS 9.4 compute")
        SystemDb_Ext(sasData, "SAS libraries", "SAS data sets")
    }

    Rel(consumer, ctrl, "HTTPS request")
    Rel(ctrl, searchOps, "Calls")
    Rel(ctrl, metaDaos, "Calls")
    Rel(ctrl, helpers, "Workspace lookup")
    Rel(ctrl, dataDaos, "Calls")
    Rel(ctrl, wsConn, "Submits code (/cmd)")
    Rel(helpers, metaDaos, "Uses")
    Rel(helpers, jdbcConn, "Creates")
    Rel(searchOps, parsers, "Uses")
    Rel(searchOps, iomConn, "Uses")
    Rel(metaDaos, parsers, "Uses")
    Rel(metaDaos, iomConn, "Uses")
    Rel(dataDaos, jdbcConn, "SQL")
    Rel(jdbcConn, wsConn, "Wraps")
    Rel(iomConn, mdServer, "Connects", "IOM")
    Rel(wsConn, wsServer, "Connects", "IOM Bridge")
    Rel(wsServer, sasData, "Reads/writes")

    UpdateRelStyle(consumer, ctrl, $offsetX="-20", $offsetY="-12")
    UpdateRelStyle(helpers, metaDaos, $offsetX="-15", $offsetY="-12")
    UpdateRelStyle(metaDaos, parsers, $offsetX="-35", $offsetY="-12")
    UpdateRelStyle(jdbcConn, wsConn, $offsetX="-20", $offsetY="-12")
    UpdateRelStyle(wsServer, sasData, $offsetX="-35", $offsetY="-15")

    UpdateLayoutConfig($c4ShapeInRow="4", $c4BoundaryInRow="1")
```

## Components

| Component | Source | Responsibilities |
|---|---|---|
| Metadata DAOs | `connections/iom/dao/*`: `RepositoriesDao`, `ServersDao`, `LibraryMetadataDao`, `PersonsDao`, `IdentityGroupsDao`, `PermissionsDao`, `TreesDao` | Read metadata (repositories, Workspace and Stored Process servers, users, groups, roles, permissions, folder trees). `LibraryMetadataDao` also **creates and deletes** library definitions. |
| Search and object operations | `connections/iom/search/**`, `connections/iom/operations/*` | `SearchDao` builds metadata XML queries from `SearchParams` using pluggable criteria (name, type, description, dates, folder, location, table…). `ObjectOperationsDao` copies, moves and deletes metadata objects, after checking `WriteMemberMetadata` permission through `AuthorizationDao`. |
| ConnectionHelpers | `connections/ConnectionHelpers` | Finds the Workspace Server host and port through `RepositoriesDao` / `ServersDao`, or uses the caller's `serverUrl`/`serverPort`. Creates `JDBCConnection` objects. |
| Metadata parsers | `connections/iom/parsers/*`, `connections/iom/search/parsers/*`, `parsers/ParserBase` | Turn the XML returned by the Metadata Server into Java models, using the `unxml` library. |
| IOMConnection | `connections/iom/IOMConnection` | Prototype-scoped. Opens an `MdOMRConnection` with the request's credentials and also provides `ISecurity` / `ISecurityAdmin` handles. Closed after each request. |
| Data DAOs | `connections/jdbc/dao/*`: `LibraryDao`, `DataSetDao`, `DataSetColumnDao`, `DataSetDataDao` | SQL against `SASHELP` dictionary views (`VLIBNAM`, tables, columns) and against data sets themselves (`SELECT`, `INSERT`, `DELETE`; a PUT "replace" is a delete followed by an insert), with paging limited by `datasetdata.max_fetch_size`. `DataSetDataDao` first submits `options VALIDVARNAME=ANY VALIDMEMNAME=EXTEND;` through the workspace session. |
| JDBCConnection | `connections/jdbc/JDBCConnection` | Wraps the workspace `IDataService` in SAS's `com.sas.rio.MVAConnection`, so there is no separate JDBC server. Closes the workspace session it owns. |
| WorkspaceConnection | `connections/workspace/WorkspaceConnection` | Opens an IOM Bridge session (`BridgeServer` / `ConnectionFactoryManager`) to the Workspace Server host and port. Submits SAS code through `ILanguageService` and returns the listing and, optionally, the log. |

## Typical request flows

* **Metadata** (e.g. `GET /sas/meta/users`): controller → `IOMConnection` → `PersonsDao` → parsers → response.
* **Data** (e.g. `GET /sas/servers/SASApp/libraries/WORK/datasets/X/data`): controller → `ConnectionHelpers` (metadata lookup of SASApp host and port) → `JDBCConnection` → `WorkspaceConnection` → `DataSetDataDao` SQL → response.
* **Code** (`PUT /sas/cmd`): controller → `ConnectionHelpers` (metadata lookup, or the caller's `serverUrl`/`serverPort`) → `WorkspaceConnection.submitSasCommand` → listing and log.
