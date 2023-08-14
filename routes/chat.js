const express = require('express');
const router = express.Router();
const query = require('../connection.js').query;

router.post('/', (req, res) => {
  const {created_by, created_at} = req.body;
  query('insert into chats (created_by, created_at) values ($1, $2) returning *', [created_by, created_at], (err, results) => {
    if (err) {throw(err)}
    const chat_id = results.rows[0].chat_id;
    query('insert into users_chats (user_id, chat_id) values ($1, $2) returning *', [created_by, chat_id], (err, results) => {
      if (err) {throw(err);}
    })

    res.status(201).json(results.rows[0]);
  })
})

router.post('/users_chats', (req, res) => {
  const {user_id, chat_id} = req.body;
  query('insert into users_chats (user_id, chat_id) values ($1, $2) returning *', [user_id, chat_id], (err, results) => {
    if (err) {throw(err)}
    res.status(201).json(results.rows[0]);
  })
})

router.get('/:id', (req, res) => {
  const user_id = parseInt(req.params.id);
  query('select * from chats join users_chats on chats.chat_id = users_chats.chat_id where users_chats.user_id = $1', [user_id], (err, results) => {
    if (err) {throw(err)}
    res.status(201).json(results.rows);
  })
})

router.get('/users_chats/:chat_id/:user_id', (req, res) => {
  const chat_id = parseInt(req.params.chat_id);
  const user_id = parseInt(req.params.user_id);
  query('select users.user_id, users.username from users join users_chats on users.user_id = users_chats.user_id where users_chats.chat_id = $1 and users_chats.user_id != $2', [chat_id, user_id], (err, results) => {
    if (err) {throw(err)}
    res.status(200).json(results.rows[0]);
  })
})

exports.router = router
