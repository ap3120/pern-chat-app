require('dotenv').config();

const express = require("express");
const cors = require("cors");

const app = express();
app.use(cors());

const PORT = process.env.PORT;

app.get('/', (req, res) => {
  res.send("Hello world");
});

app.listen(PORT, () => {
  console.log(`App is running on port ${PORT}`)
})
