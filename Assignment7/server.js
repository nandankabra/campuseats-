// CampusEats Menu service: GraphQL schema and SQL resolvers.
const { ApolloServer } = require('@apollo/server');
const { startStandaloneServer } = require('@apollo/server/standalone');
const fs = require('node:fs');
const path = require('node:path');
const pool = require('./db');

const typeDefs = fs.readFileSync(path.join(__dirname, 'schema.graphql'), 'utf8');
const resolvers = {
  Query: {
    dishes: async () => {
      const [rows] = await pool.query('SELECT * FROM dishes ORDER BY id');
      return rows;
    },
    dish: async (_parent, { id }) => {
      const [rows] = await pool.query('SELECT * FROM dishes WHERE id = ?', [id]);
      return rows[0] || null;
    },
  },
  Mutation: {
    addDish: async (_parent, { input }) => {
      const { name, price, available } = input;
      const [result] = await pool.query(
        'INSERT INTO dishes (name, price, available) VALUES (?, ?, ?)',
        [name, price, available]
      );
      const [rows] = await pool.query('SELECT * FROM dishes WHERE id = ?', [result.insertId]);
      return rows[0];
    },
  },
};

async function start() {
  // Fail before listening if the database is not configured.
  await pool.query('SELECT 1');
  const server = new ApolloServer({ typeDefs, resolvers });
  const port = Number(process.env.PORT || 4007);
  const { url } = await startStandaloneServer(server, { listen: { port } });
  console.log(`CampusEats GraphQL API ready at ${url}`);
}

start().catch(async (error) => {
  console.error('Failed to start server:', error.message);
  await pool.end();
  process.exitCode = 1;
});
