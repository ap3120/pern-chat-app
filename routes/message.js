const express = require('express');
const router = express.Router();
const query = require('../connection.js').query;

router.post('/', (req, res) => {
  const {content, sender_id, sender_username, receiver_id, receiver_username, chat_id, send_at} = req.body;
  console.log("message route")
  console.log(content, sender_id, sender_username, receiver_id, receiver_username, chat_id, send_at);
  query('insert into messages (content, sender_id, sender_username, receiver_id, receiver_username, chat_id, send_at) values($1, $2, $3, $4, $5, $6, $7) returning *', [
      content,
      sender_id,
      sender_username,
      receiver_id,
      receiver_username,
      chat_id,
      send_at
    ], (err, results) => {
      if (err) {throw(err)}
      res.status(201).json(results.rows[0]);
    })
})

router.get('/:chat_id', (req, res) => {
  const chat_id = parseInt(req.params.chat_id);
  console.log(chat_id);
  query('select * from messages where chat_id = $1 order by send_at', [chat_id], (err, results) => {
    if (err) {throw(err)}
    res.status(200).json(results.rows);
  })
})

exports.router = router;
