require('dotenv').config();
const Pool = require('pg').Pool

const user = process.env.DB_USER;
const database = process.env.DB_NAME;
const password = process.env.DB_PASSWORD;
const PORT = process.env.PORT;

const pool = new Pool({
  user: user,
  host: 'localhost',
  database: database,
  password: password,
  port: PORT,
})

module.exports = {
  query: (text, params, callback) => {
    return pool.query(text, params, callback);
  },
}
