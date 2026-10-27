// One shared connection pool for the CampusEats Menu database.
const mysql = require('mysql2/promise');
const path = require('node:path');
require('dotenv').config({ path: path.join(__dirname, '.env') });

module.exports = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'campuseats_a07',
  port: Number(process.env.DB_PORT || 3306),
  decimalNumbers: true,
  waitForConnections: true,
  connectionLimit: 10,
});
