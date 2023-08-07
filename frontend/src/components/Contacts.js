import { List, ListItem, ListItemText } from '@mui/material';
import {useState, useEffect} from 'react';
import {Navbar} from './Navbar';
import {SearchUserInput} from './SearchUserInput';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';

export const Contacts = () => {

  const [filteringUsers, setFilteringUsers] = useState(false);
  const [users, setUsers] = useState([]);
  const [chats, setChats] = useState([]);

  const getChats = async() => {
    try {
      const response = await fetch(`http://localhost:3000/chat/${sessionStorage.getItem('user_id')}`);
      const jsonResponse = await response.json();
      console.log(jsonResponse);
      setChats(jsonResponse);
    } catch (error) {
      console.log(error);
    }
  }

  const getChatContact = async(chat_id) => {
    try {
      const response = await fetch(`http://localhost:3000/chat/users_chats/${chat_id}/${sessionStorage.getItem("user_id")}`)
      const jsonResponse = await response.json();
      console.log(jsonResponse);
      return jsonResponse.username;
    } catch(error) {
      console.log(error);
    }
  }

  useEffect(() => {
    getChats();
  }, [])

  return (
    <div style={{width:'500px'}}>
      <Navbar setFilteringUsers={setFilteringUsers} setUsers={setUsers}/>
      {filteringUsers && <SearchUserInput users={users}/>}
      <List>
      {chats.map((chat, index) => (
        <ListItem key={index}>
          <AccountCircleIcon sx={{fontSize:'50px', marginRight:'20px'}}/>
          <ListItemText>{getChatContact(chat.chat_id)}</ListItemText>
        </ListItem>
      ))}
      </List>


    </div>
  )
}
