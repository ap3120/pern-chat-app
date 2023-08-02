require('dotenv').config();

const express = require("express");
const cors = require("cors");
const bodyParser = require('body-parser');
const session = require('express-session');
const userRouter = require('./routes/user.js').router;
const chatRouter = require('./routes/chat.js').router;
const app = express();

const PORT = process.env.PORT;

app.use(cors());
app.use(express.json());
app.use(bodyParser.json());
app.use(express.urlencoded({extended: false}));
// Session middleware
const store = new session.MemoryStore();
app.use(session({
    secret: process.env.SESSION_SECRET,
    cookie: {
        maxAge: 1000*60*60*24,
        secure: false,
        sameSite: 'none'
    },
    resave: false,
    saveUninitialized: false,
    store
}));

// routes
app.use('/', userRouter);
//app.use('/chat', chatRouter);

app.get('/', (req, res) => {
  res.json({msg: "Hello world"});
});

app.listen(PORT, () => {
  console.log(`App is running on port ${PORT}`)
})
