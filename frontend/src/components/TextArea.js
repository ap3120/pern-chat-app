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
export const TextArea = ({contact, setMessages}) => {

  const [message, setMessage] = useState('');

  const handleClick = async() => {
    try {
      const response = await fetch('http://localhost:3000/message', {
        method: 'POST',
        body: JSON.stringify({
          content: message,
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

  return (
    <div style={{display:'flex', alignItems:'end'}}>
      <StyledTextArea
        value={message}
        maxRows={4}
        onChange={(e) => setMessage(e.target.value)}
      />
      <IconButton onClick={handleClick}>
        <SendIcon/>
      </IconButton>
    </div>
  )
}
