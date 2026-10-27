# CS543 Assignment 7 - CampusEats Menu Service

**Group 8**

**How to run:** From `lab/Assignment7`, configure `.env` using `.env.example`, run `mysql -u root -p < schema.sql`, run `npm install && npm start`, then open `public/index.html` with VS Code Live Server.

## Team members

| Name | Roll number |
| --- | --- |
| Priya | 20251651073 |
| Nandan Kabra | 20251651060 |
| Neeraj Sharma | 20251651063 |
| Abhinav Gupta | 20251651002 |

## Repository and local demonstration files

- **GitHub repository:** [nandankabra/campuseats-](https://github.com/nandankabra/campuseats-), folder `Assignment7`.
- **Local demonstration video:** [assets/demonstration.mp4](assets/demonstration.mp4).
- **Local screenshots:** [Before the mutation](assets/before.png) and [after the mutation](assets/after.png).

The video and both screenshots are stored locally in this assignment's `assets/` folder. The repository URL comes from this project's configured Git remote. The updated assignment files are local and still need to be committed and pushed before submitting one repository link per team through Google Classroom.

## Deliverables

| Requirement | File |
| --- | --- |
| Object type, Query, Mutation, and mutation input | [schema.graphql](schema.graphql) |
| Apollo Server and SQL resolvers | [server.js](server.js) |
| MySQL connection pool | [db.js](db.js) |
| One table and four seed rows | [schema.sql](schema.sql) |
| Page that reads and adds dishes through GraphQL | [public/index.html](public/index.html) |
| Page styling | [public/style.css](public/style.css) |
| Database configuration template | [.env.example](.env.example) |
| Screenshot before the mutation | [assets/before.png](assets/before.png) |
| Screenshot after the mutation | [assets/after.png](assets/after.png) |
| Demonstration video | [assets/demonstration.mp4](assets/demonstration.mp4) |

The 48-second video shows actual VS Code screens for `schema.graphql`, `server.js`, `db.js`, `schema.sql`, and `public/index.html`, followed by the initial menu, entering Paneer Roll and its price, the successful mutation, and the menu after refresh. Captions show the GraphQL API port **4007**, web page port **5500**, and MySQL port **3306**. It is a silent step-by-step capture video rather than a continuous screen recording.

## Run details

- Tested with Node.js 22 and a local MySQL Server.
- Set your MySQL credentials in `.env`; use database `campuseats_a07` and API port `4007` as shown in `.env.example`.
- `schema.sql` creates one `dishes` table and seeds Veg Sandwich, Masala Dosa, Veg Biryani, and Lemonade only when the table is empty.
- API and Apollo Sandbox: [http://localhost:4007/](http://localhost:4007/).
- With the parent `webservices` folder open in VS Code, the Live Server page is normally [http://127.0.0.1:5500/lab/Assignment7/public/index.html](http://127.0.0.1:5500/lab/Assignment7/public/index.html).
- Keep the API running while using the page. If you change its port, update `API` in `public/index.html` to match. Port 4007 lets this assignment run alongside the tutorial on port 4000.

## Answers required by the assignment

### 1. Which CampusEats service did you pick?

The **Menu service**. One `Dish` object maps to the `dishes` table with `id`, `name`, `price`, and `available` columns.

### 2. Name one field a client can skip and one it cannot. Why use `!`?

A client can skip `available` when selecting query results, for example:

```graphql
query { dishes { id name price } }
```

For mutation input, it cannot skip `name` or `price`: they are declared as `String!` and `Float!`, without defaults. GraphQL rejects the mutation if either is omitted or null. Input `available` can be omitted because it defaults to `true`.

`!` means **non-null**. On output fields, it guarantees a non-null result when the field is selected; it does not force the client to select that field. On input fields without defaults, it requires the client to provide a non-null value.

### 3. Which part of your query became WHERE, and which resolver ran INSERT?

For this query:

```graphql
query { dish(id: "1") { id name price available } }
```

`Query.dish` binds the `id` argument to the `?` in:

```sql
SELECT * FROM dishes WHERE id = ?;
```

The list query `dishes` uses `SELECT * FROM dishes ORDER BY id` and has no `WHERE` clause. GraphQL field selection controls the response fields, not the SQL `WHERE` condition.

This mutation calls the `Mutation.addDish` resolver:

```graphql
mutation {
  addDish(input: { name: "Paneer Roll", price: 90 }) {
    id name price
  }
}
```

The resolver executes:

```sql
INSERT INTO dishes (name, price, available) VALUES (?, ?, ?);
```

It then selects the new row using `result.insertId` and returns it. Each successful mutation inserts a new record.

## Verification

Verified against the running API and MySQL: dish listing, lookup by ID, missing ID, required inputs, default availability, and adding Paneer Roll through the page. The record remains after refresh, as shown in the screenshots and video.

The existing `Assignment7_Report.pdf`, `create_report_pdf.py`, and `assets/screen_*` files belong to the earlier Books and Authors tutorial. Use the CampusEats deliverables listed above for this assignment.
