import { Typography, Box } from "@mui/material";
import { useEffect, useRef, useCallback } from "react";
import dayjs from "dayjs";

export const MessageContainer = ({contact, messages, setMessages}) => {

  const ref = useRef();

  const getStyle = (msg) => {
    return {
      p: 1,
      borderRadius: '15px',
      marginLeft: msg.receiver_id === parseInt(sessionStorage.getItem('user_id')) ? 0: 'auto',
      marginRight: msg.receiver_id === parseInt(sessionStorage.getItem('user_id')) ? 'auto' : 0,
      marginBottom: '10px',
      width: 'fit-content',
      backgroundColor: msg.receiver_id === parseInt(sessionStorage.getItem('user_id')) ? 'secondary.main' : 'primary.main',
      color: msg.receiver_id === parseInt(sessionStorage.getItem('user_id')) ? 'secondary.contrastText' : 'primary.contrastText',
    }
  }

  const PORT = process.env.REACT_APP_SERVER_PORT;
  const getMessages = useCallback(async() => {
    try {
      const response = await fetch(
          `http://localhost:${PORT}/message/${contact.chat_id}`,
          {
            credentials: 'include',
          }
      );
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
            <Typography data-testid={`message-${index}`} variant='body2' key={i} sx={{overflowWrap:'break-word', wordBreak:'break-word'}}>{elem}</Typography>
          ))}
          <Typography sx={{textAlign:'right', fontSize:'0.7rem'}}>{dayjs(msg.send_at).format('HH:mm, ddd DD MMM YYYY')}</Typography>
        </Box>
      ))}
    </Box>
  )
}
