import { useState, useRef, useEffect } from 'react';
import {TextArea} from './TextArea';
import {MessageContainer} from './MessageContainer';
import { AppBar, Box, Toolbar, Typography } from '@mui/material';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';

export const Chat = ({contact}) => {
  const [messages, setMessages] = useState([]);
  const inputRef = useRef();

  useEffect(() => {
    if (inputRef.current) inputRef.current.focus();
  }, [contact]);

  return (
    <Box sx={{ maxHeight:'100vh', borderLeft: '2px solid', borderColor:'text.main', flexGrow:1, display:'flex', flexDirection:'column', justifyContent:'space-between', backgroundColor:'background.main'}}>
      {contact.user_id &&
        <>
          <AppBar position='static'>
            <Toolbar>
              <AccountCircleIcon sx={{fontSize:'50px', marginRight:'20px'}}/>
              <Typography>{contact.username}</Typography>
            </Toolbar>
          </AppBar>
          <MessageContainer contact={contact} messages={messages} setMessages={setMessages}/>
          <TextArea inputRef={inputRef} contact={contact} setMessages={setMessages}/>
        </>
      }
    </Box>
  )
}
