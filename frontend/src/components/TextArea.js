import { useState } from "react";
import { TextareaAutosize, IconButton } from "@mui/material";
import { styled } from "@mui/system";
import SendIcon from '@mui/icons-material/Send';

const StyledTextArea = styled(TextareaAutosize) (
  () => `
width: 90%;
font-size: 1rem;
line-height: 1.5;
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
    <div style={{display:'flex', alignItems:'end'}}>
      <StyledTextArea
        ref={inputRef}
        value={message}
        maxRows={4}
        onKeyDown={(e) => handleKeyDown(e)}
        onChange={(e) => setMessage(e.target.value)}
      />
      <IconButton onClick={handleClick}>
        <SendIcon/>
      </IconButton>
    </div>
  )
}
