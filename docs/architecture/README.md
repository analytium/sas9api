# SAS9API architecture (C4)

Architecture views of SAS9API using the [C4 model](https://c4model.com/), written as [Mermaid C4 diagrams](https://mermaid.js.org/syntax/c4.html) so they render on GitHub and live next to the code.

| Level | View | What it shows |
|---|---|---|
| 1 | [System Context](01-system-context.md) | SAS9API, its users, and the SAS 9.4 platform it fronts |
| 2 | [Containers](02-container.md) | The Spring Boot API, Swagger UI, configuration and logs |
| 3 | [Components: request handling](03a-components-request-handling.md) | Transport, auth interceptor, connection resolution, controller |
| 3 | [Components: SAS connectivity](03b-components-sas-connectivity.md) | Metadata DAOs over IOM, data DAOs over JDBC/Workspace |
| – | [Risks](04-risks.md) | Security and architecture risks found while mapping the code |

The Level 3 view is split in two to keep each diagram under about 15 elements, which is roughly where Mermaid's C4 layout stops being readable.

## Conventions

* **Same alias, same thing.** Mermaid C4 has no shared model, so each diagram declares its own elements. Keep the aliases and labels identical across files: `consumer`, `clientApp`, `operator`, `sas9api`, `api`, `swagger`, `config`, `logs`, `mdServer`, `wsServer`, `sasData`, `ctrl`, `helpers`.
* Every relationship states a protocol where one exists (HTTPS, IOM, IOM Bridge).
* Each diagram is followed by a table naming the source classes or files behind each element. Update the table when the code moves.
* Classes that exist but don't run (dead or disabled code) are listed under the diagram, not drawn in it.

## Editing and checking diagrams

Mermaid's C4 support is still experimental, and its layout follows a few rules that aren't obvious:

* Shapes are placed in declaration order, `$c4ShapeInRow` per row. **Loose elements (outside any boundary) are always drawn first, at the top**, and boundaries stack below them (`$c4BoundaryInRow="1"`). These diagrams use that for a top-to-bottom flow: users at the top, SAS9API in the middle, the `SAS 9.4 platform` boundary at the bottom.
* A boundary's first row continues the previous boundary's last-row count. For example, if the boundary above ended with 2 shapes and `$c4ShapeInRow="3"`, the next boundary's first row holds only 1 shape. Check for this when adding or removing elements.
* A row also wraps when it would exceed the **viewer's screen width** (`screen.availWidth`). The layouts are tuned for desktop screens (1280px and up). On narrow screens and in headless tools with an 800px default screen, rows wrap earlier.
* Overlapping labels can only be moved with `UpdateRelStyle(from, to, $offsetX=…, $offsetY=…)`. A label starts at the midpoint of its line, so a negative `$offsetX` of about half the label's width centres it.

To preview, paste a block into <https://mermaid.live>. To check syntax locally:

```bash
npx -p @mermaid-js/mermaid-cli@11.4.2 mmdc -i docs/architecture/02-container.md -o /tmp/c4.md
```

Given a Markdown file, `mmdc` renders every `mermaid` block to an SVG next to the output file, and fails on any syntax error. Its headless browser has an 800px screen, so judge the layout in mermaid.live or on GitHub, not from these SVGs.
