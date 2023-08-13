import { Typography } from "@mui/material";
import { useEffect } from "react";

export const MessageContainer = ({contact, messages, setMessages}) => {

  const getStyle = (msg) => {
    return {
      marginLeft: msg.sender_id == sessionStorage.getItem('user_id') ? 'auto' : 0,
      marginRight: msg.sender_id == sessionStorage.getItem('user_id') ? 0 : 'auto',
      border: '1px solid blue',
      marginBottom: '10px',
      width: 'fit-content',
    }
  }
  const getMessages = async() => {
    try {
      console.log(contact.chat_id);
      const response = await fetch(`http://localhost:3000/message/${contact.chat_id}`);
      const jsonResponse = await response.json();
      console.log(jsonResponse)
      setMessages(jsonResponse);
    } catch (error) {
      console.log(error);
    }
  }

  useEffect(() => {
    getMessages();
  }, [contact])

  return (

    <div style={{width:'100%', border:'1px solid green', padding:10, overflowX:'hidden', flexGrow:1, overflowY:'scroll'}}>
      {messages.map((msg, index) => (
        <div key={index} style={getStyle(msg)}>
          <Typography sx={{overflowWrap:'break-word', wordBreak: 'break-word'}}>{msg.content}</Typography>
          <Typography>{msg.send_at}</Typography>
        </div>
      ))}
    </div>
  )
}
