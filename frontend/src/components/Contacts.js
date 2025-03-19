import { List, ListItem, ListItemText, Box } from '@mui/material';
import {useState, useEffect} from 'react';
import {Navbar} from './Navbar';
import {SearchUserInput} from './SearchUserInput';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';

export const Contacts = ({setContact, closeSocket}) => {

  const [filteringUsers, setFilteringUsers] = useState(false);
  const [users, setUsers] = useState([]);
  const [chats, setChats] = useState([]);
  const [selectedIndex, setSelectedIndex] = useState(0);

  const PORT = process.env.REACT_APP_PORT;

  const getStyle = i => {
    return [
      {
        color: 'text.main',
        cursor: 'pointer',
        backgroundColor: i === selectedIndex ? 'background.hover' : 'background.main',
        borderRadius: '15px',
        mb: 1,
      },
      (theme) => ({
        '&:hover': {
          backgroundColor: theme.palette.background.hover,
        },
      }),
    ]
  }

  const getChats = async() => {
    try {
      const response = await fetch(`http://localhost:${PORT}/chat/${sessionStorage.getItem('user_id')}`);
      const jsonResponse = await response.json();
      setChats(jsonResponse);
      for (let i=0; i<jsonResponse.length; i++) {
        getChatContact(jsonResponse[i].chat_id);
      }
    } catch (error) {
      console.log(error);
    }
  }

  const getChatContact = async(chat_id) => {
    try {
      const response = await fetch(`http://localhost:${PORT}/chat/users_chats/${chat_id}/${sessionStorage.getItem("user_id")}`)
      const jsonResponse = await response.json();
      setChats(prevChats => prevChats.map(elem => (elem.chat_id === chat_id ? {...elem, chat_contact: jsonResponse.username, chat_contact_id: jsonResponse.user_id} : elem)))
    } catch(error) {
      console.log(error);
    }
  }

  const handleClick = (chat, index) => {
    setSelectedIndex(index);
    setContact({username: chat.chat_contact, user_id: chat.chat_contact_id, chat_id: chat.chat_id})
  }

  useEffect(() => {
    getChats();
  }, [filteringUsers])

  return (
    <Box sx={{width:'20vw', minWidth:'400px', backgroundColor:'background.main'}}>
      <Navbar setFilteringUsers={setFilteringUsers} setUsers={setUsers} closeSocket={closeSocket} />
      {filteringUsers &&
        <Box sx={{p:2}}>
          <SearchUserInput users={users} setChats={setChats} setFilteringUsers={setFilteringUsers}/>
        </Box>
      }
      <List sx={{p: 2}}>
      {chats.map((chat, index) => (
        <ListItem sx={getStyle(index)} key={index} onClick={() => handleClick(chat, index)}>
          <AccountCircleIcon sx={{fontSize:'50px', marginRight:'20px'}}/>
          <ListItemText>{chat.chat_contact}</ListItemText>
        </ListItem>
      ))}
      </List>
    </Box>
  )
}
