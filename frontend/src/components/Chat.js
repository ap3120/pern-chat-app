import { useState } from 'react';
import {TextArea} from './TextArea';
import {MessageContainer} from './MessageContainer';
import { AppBar, Toolbar, Typography } from '@mui/material';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';

export const Chat = ({contact}) => {
  const [messages, setMessages] = useState([]);

  return (
    <div style={{border: '1px solid red', flexGrow:1, display:'flex', flexDirection:'column', justifyContent:'space-between'}}>
      <div>
        {contact.user_id &&
          <>
            <AppBar position='static'>
              <Toolbar>
                <AccountCircleIcon sx={{fontSize:'50px', marginRight:'20px'}}/>
                <Typography>{contact.username}</Typography>
              </Toolbar>
            </AppBar>
            <MessageContainer contact={contact} messages={messages} setMessages={setMessages}/>
          </>
        }
      </div>
      <div>
        {contact.user_id &&
          <TextArea contact={contact} setMessages={setMessages}/>
        }
      </div>
    </div>
  )
}
