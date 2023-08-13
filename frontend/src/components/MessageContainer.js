import { Typography } from "@mui/material";
import { useEffect, useRef } from "react";

export const MessageContainer = ({contact, messages, setMessages}) => {

  const ref = useRef();

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
      const response = await fetch(`http://localhost:3000/message/${contact.chat_id}`);
      const jsonResponse = await response.json();
      setMessages(jsonResponse);
    } catch (error) {
      console.log(error);
    }
  }

  useEffect(() => {
    getMessages();
  }, [contact])

  useEffect(() => {
    ref.current.scrollTop = ref.current.scrollHeight;
  }, [messages]);

  return (

    <div ref={ref} style={{width:'100%', border:'1px solid green', padding:10, overflowX:'hidden', flexGrow:1, overflowY:'scroll'}}>
      {messages.map((msg, index) => (
        <div key={index} style={getStyle(msg)}>
          {/*<Typography sx={{overflowWrap:'break-word', wordBreak: 'break-word'}}>{msg.content}</Typography>*/}
          {msg.content.split('<br />').map((elem, i) => (
            <Typography key={i} sx={{overflowWrap:'break-word', wordBreak:'break-word'}}>{elem}</Typography>
          ))}
          <Typography>{msg.send_at}</Typography>
        </div>
      ))}
    </div>
  )
}
