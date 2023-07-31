require('dotenv').config();

const express = require("express");
const cors = require("cors");
const userRouter = require('./routes/user.js').router;
const chatRouter = require('./routes/chat.js').router;
const app = express();
app.use(cors());

const PORT = process.env.PORT;

// routes
app.use('/', userRouter);
app.use('/chat', chatRouter);

app.get('/', (req, res) => {
  res.json({msg: "Hello world"});
});

app.listen(PORT, () => {
  console.log(`App is running on port ${PORT}`)
})
