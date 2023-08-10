import { List, ListItem, ListItemText } from '@mui/material';
import {useState, useEffect} from 'react';
import {Navbar} from './Navbar';
import {SearchUserInput} from './SearchUserInput';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';

export const Contacts = ({setContact}) => {

  const [filteringUsers, setFilteringUsers] = useState(false);
  const [users, setUsers] = useState([]);
  const [chats, setChats] = useState([]);

  const getChats = async() => {
    try {
      const response = await fetch(`http://localhost:3000/chat/${sessionStorage.getItem('user_id')}`);
      const jsonResponse = await response.json();
      console.log(jsonResponse);
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
      const response = await fetch(`http://localhost:3000/chat/users_chats/${chat_id}/${sessionStorage.getItem("user_id")}`)
      const jsonResponse = await response.json();
      console.log(jsonResponse);
      setChats(prevChats => prevChats.map(elem => (elem.chat_id === chat_id ? {...elem, chat_contact: jsonResponse.username, chat_contact_id: jsonResponse.user_id} : elem)))
    } catch(error) {
      console.log(error);
    }
  }

  const handleClick = chat => {
    setContact({username: chat.chat_contact, user_id: chat.chat_contact_id, chat_id: chat.chat_id})
  }

  useEffect(() => {
    getChats();
  }, [filteringUsers])

  return (
    <div style={{width:'500px'}}>
      <Navbar setFilteringUsers={setFilteringUsers} setUsers={setUsers}/>
      {filteringUsers && <SearchUserInput users={users} setChats={setChats} setFilteringUsers={setFilteringUsers}/>}
      <List>
      {chats.map((chat, index) => (
        <ListItem key={index} onClick={() => handleClick(chat)}>
          <AccountCircleIcon sx={{fontSize:'50px', marginRight:'20px'}}/>
          <ListItemText>{chat.chat_contact}</ListItemText>
        </ListItem>
      ))}
      </List>


    </div>
  )
}
