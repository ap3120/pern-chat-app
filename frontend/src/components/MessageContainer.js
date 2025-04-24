import { Typography, Box } from "@mui/material";
import { useEffect, useRef, useCallback } from "react";
import dayjs from "dayjs";

export const MessageContainer = ({contact, messages, setMessages}) => {

  const ref = useRef();

  const getStyle = (msg) => {
    return {
      p: 1,
      borderRadius: '15px',
      marginLeft: msg.sender_id === parseInt(sessionStorage.getItem('user_id')) ? 'auto' : 0,
      marginRight: msg.sender_id === parseInt(sessionStorage.getItem('user_id')) ? 0 : 'auto',
      marginBottom: '10px',
      width: 'fit-content',
      backgroundColor: msg.sender_id === parseInt(sessionStorage.getItem('user_id')) ? 'primary.main' : 'secondary.main',
      color: msg.sender_id === parseInt(sessionStorage.getItem('user_id')) ? 'primary.contrastText' : 'secondary.contrastText',
    }
  }

  const PORT = process.env.REACT_APP_PORT;
  const getMessages = useCallback(async() => {
    try {
      const response = await fetch(`http://localhost:${PORT}/message/${contact.chat_id}`);
      const jsonResponse = await response.json();
      setMessages(jsonResponse);
    } catch (error) {
      console.log(error);
    }
  }, [PORT, contact.chat_id, setMessages]);

  useEffect(() => {
    getMessages();
  }, [contact])

  useEffect(() => {
    ref.current.scrollTop = ref.current.scrollHeight;
  }, [messages]);

  return (

    <Box ref={ref} sx={{p: 2, width:'100%', backgroundColor:'background.main', overflowX:'hidden', flexGrow:1, overflowY:'scroll'}}>
      {messages.map((msg, index) => (
        <Box key={index} sx={getStyle(msg)}>
          {msg.content.split('<br />').map((elem, i) => (
            <Typography variant='body2' key={i} sx={{overflowWrap:'break-word', wordBreak:'break-word'}}>{elem}</Typography>
          ))}
          <Typography sx={{textAlign:'right', fontSize:'0.7rem'}}>{dayjs(msg.send_at).format('HH:mm, ddd DD MMM YYYY')}</Typography>
        </Box>
      ))}
    </Box>
  )
}
