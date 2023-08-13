import { useState } from "react";
import { TextareaAutosize, IconButton, Box } from "@mui/material";
import { styled } from "@mui/system";
import SendIcon from '@mui/icons-material/Send';

const StyledTextArea = styled(TextareaAutosize) (
  ({theme}) => `
width: 90%;
font-size: 1rem;
line-height: 1.5;
background: transparent;
color: ${theme.palette.text.main};

&:focus {
border-color: ${theme.palette.primary.main};
box-shadow: 0 0 0 0;
outline: 0;
}
`
)
export const TextArea = ({inputRef, contact, setMessages}) => {

  const [message, setMessage] = useState('');

  const handleClick = async() => {
    const msg = message.replace(/[\n]/g, '<br />');
    console.log(msg);
    try {
      const response = await fetch('http://localhost:3000/message', {
        method: 'POST',
        body: JSON.stringify({
          content: msg,
          sender_id: sessionStorage.getItem('user_id'),
          sender_username: sessionStorage.getItem('username'),
          receiver_id: contact.user_id,
          receiver_username: contact.username,
          chat_id: contact.chat_id,
          send_at: new Date(),
        }),
        headers: {'Content-Type': 'application/json'}
      })
      const jsonResponse = await response.json();
      setMessage('');
      setMessages(prevMessages => [...prevMessages, jsonResponse])
    } catch(error) {
      console.log(error);
    }
  }

  const handleKeyDown = e => {
    if (e.keyCode === 13 && !e.shiftKey) {
      handleClick();
    } else if (e.keyCode === 13 && e.shiftKey) {
      e.preventDefault();
      setMessage(prevMessage => prevMessage + '\n');
    } else {
      return;
    }
  }

  return (
    <Box sx={{pb: 2, display:'flex', justifyContent:'center', alignItems:'end', backgroundColor:'background.main'}}>
      <StyledTextArea
        ref={inputRef}
        value={message}
        maxRows={4}
        onKeyDown={(e) => handleKeyDown(e)}
        onChange={(e) => setMessage(e.target.value)}
      />
      <IconButton onClick={handleClick}>
        <SendIcon sx={{color: 'text.main'}}/>
      </IconButton>
    </Box>
  )
}
