// server.js -- the GraphQL API
const { ApolloServer } = require('@apollo/server');
const { startStandaloneServer } = require('@apollo/server/standalone');
require('dotenv').config();
const pool = require('./db');

// 1. the schema: what the API offers
const typeDefs = `#graphql
  type Author {
    id: ID!
    name: String!
    country: String
    books: [Book!]!
  }

  type Book {
    id: ID!
    title: String!
    year: Int
    author: Author!
  }

  type Query {
    authors: [Author!]!
    author(id: ID!): Author
    books(year: Int): [Book!]!
  }

  type Mutation {
    addBook(title: String!, year: Int, authorId: ID!): Book!
    deleteBook(id: ID!): Boolean
  }
`;

// 2. the resolvers: how each field gets its data from MySQL
const resolvers = {
  Query: {
    authors: async () => {
      const [rows] = await pool.query('SELECT * FROM authors');
      return rows;
    },
    author: async (_parent, { id }) => {
      const [rows] = await pool.query('SELECT * FROM authors WHERE id = ?', [id]);
      return rows[0] || null;
    },
    books: async (_parent, { year }) => {
      if (year !== undefined && year !== null) {
        const [rows] = await pool.query('SELECT * FROM books WHERE year = ?', [year]);
        return rows;
      }
      const [rows] = await pool.query('SELECT * FROM books');
      return rows;
    },
  },

  // relationship fields -- these are the ones you must write
  Author: {
    books: async (author) => {
      const [rows] = await pool.query('SELECT * FROM books WHERE author_id = ?', [author.id]);
      return rows;
    },
  },

  Book: {
    author: async (book) => {
      const [rows] = await pool.query('SELECT * FROM authors WHERE id = ?', [book.author_id]);
      return rows[0] || null;
    },
  },

  Mutation: {
    addBook: async (_parent, { title, year, authorId }) => {
      const [result] = await pool.query(
        'INSERT INTO books (title, year, author_id) VALUES (?, ?, ?)',
        [title, year || null, authorId]
      );
      const [rows] = await pool.query('SELECT * FROM books WHERE id = ?', [result.insertId]);
      return rows[0];
    },
    deleteBook: async (_parent, { id }) => {
      const [result] = await pool.query('DELETE FROM books WHERE id = ?', [id]);
      return result.affectedRows > 0;
    },
  },
};

// 3. start the server
async function start() {
  const port = process.env.PORT ? Number(process.env.PORT) : 4000;
  const server = new ApolloServer({ typeDefs, resolvers });
  const { url } = await startStandaloneServer(server, { listen: { port } });
  console.log(`GraphQL API ready at ${url}`);
}

start().catch((err) => {
  console.error('Failed to start server:', err);
});
