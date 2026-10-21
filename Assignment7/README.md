# Campus Library · Hands-On GraphQL Tutorial

This project implements **Assignment 7 / Hands-on Tutorial 07 (CS543 - Web Services)**: an end-to-end web application using **HTML/CSS/JS**, a **GraphQL API (Node.js + Apollo Server)**, and a **MySQL** database.

---

## 🏛️ Architecture

```text
Browser (Front-End)           GraphQL API (Node + Apollo)            MySQL (Database)
HTML + CSS + fetch()   ────►  Apollo Server v4 (:4000)        ────►  gqldemo
                              - Schemas & Types                      - authors table
                              - Query & Mutation Resolvers           - books table
```

1. **Frontend**: Sends GraphQL queries and mutations via standard HTTP `POST` requests (`fetch()`).
2. **GraphQL API**: Resolves queries and nested relationships without over-fetching or under-fetching.
3. **MySQL**: Relational storage linking authors and books via foreign keys.

---

## 📂 Project Structure

```text
Assignment7/
├── CS543_T07.pdf        # Tutorial specification document
├── schema.sql           # Database schema & sample seed data
├── db.js                # MySQL connection pool configuration
├── server.js            # GraphQL schema (typeDefs) and resolvers
├── package.json         # Node.js dependencies and scripts
├── .env.example         # Environment variables template
├── public/
│   ├── index.html       # Web interface (authors list & add book form)
│   └── style.css        # Card & form styling
└── README.md            # Setup and execution documentation
```

---

## 🚀 Setup & Execution Guide

### Step 1: Initialize the MySQL Database

Ensure your MySQL server is running. Log in to MySQL and execute the `schema.sql` script:

```bash
mysql -u root -p < schema.sql
```

Or within the MySQL CLI:

```sql
SOURCE schema.sql;
SHOW TABLES;
SELECT * FROM authors;
SELECT * FROM books;
```

This creates:
- Database: `gqldemo`
- Table `authors`: `id`, `name`, `country`
- Table `books`: `id`, `title`, `year`, `author_id` (foreign key to `authors.id`)
- Initial seed records for *Asha Patel* and *Ravi Kumar*.

---

### Step 2: Configure Environment Variables

Create your `.env` file from the provided template:

```bash
cp .env.example .env
```

Edit `.env` to match your local MySQL credentials:

```env
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=YOUR_MYSQL_PASSWORD
DB_NAME=gqldemo
PORT=4000
```

---

### Step 3: Install Dependencies

```bash
npm install
```

Installed packages:
- `@apollo/server`: Apollo Server v4 standalone GraphQL server
- `graphql`: Core GraphQL query language execution engine
- `mysql2`: MySQL client supporting Promises and connection pooling
- `dotenv`: Loads environment configurations

---

### Step 4: Start the GraphQL Server

```bash
npm start
```

Or for automatic reloads during development:

```bash
npm run dev
```

You will see:
```text
GraphQL API ready at http://localhost:4000/
```

---

## 🔍 Testing with Apollo Sandbox

Open [http://localhost:4000/](http://localhost:4000/) in your web browser. Apollo Server provides an embedded **Apollo Sandbox** query explorer.

### 1. Fetch Authors with Nested Books (Query)
```graphql
query GetAllAuthorsAndBooks {
  authors {
    id
    name
    country
    books {
      id
      title
      year
    }
  }
}
```

### 2. Add a New Book (Mutation)
```graphql
mutation CreateBook {
  addBook(title: "GraphQL in Practice", year: 2025, authorId: "1") {
    id
    title
    year
    author {
      id
      name
    }
  }
}
```

### 3. Filter Books by Year (Query)
```graphql
query FilterBooksByYear {
  books(year: 2025) {
    id
    title
    year
    author {
      name
    }
  }
}
```

### 4. Delete a Book (Mutation)
```graphql
mutation RemoveBook {
  deleteBook(id: "3")
}
```

---

## 🌐 Running the Web Application

With `server.js` running:

1. Open `public/index.html` in your browser, or use the **VS Code Live Server** extension (right-click `public/index.html` → **Open with Live Server** at `http://127.0.0.1:5500`).
2. The page fetches all authors and nested books in a single request.
3. Submitting the "Add a book" form executes the GraphQL mutation and immediately refreshes the displayed list.
