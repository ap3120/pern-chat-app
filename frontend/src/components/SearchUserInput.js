import { TextField, Autocomplete } from '@mui/material';
import {useState} from 'react';
export const SearchUserInput = ({users, setChats, setFilteringUsers}) => {

  const [value, setValue] = useState('');
  const PORT = process.env.REACT_APP_SERVER_PORT;

  const arr = users.map(elem => elem.username);

  const handleChange = async(e, v) => {
    const index = users.findIndex(elem => elem.username === v);
    if (index === -1) {return;}
    setValue(v);
    try {
      const response = await fetch(`http://localhost:${PORT}/chat`, {
        method: 'POST',
        credentials: 'include',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          created_by: sessionStorage.getItem('user_id')
        })
      })
      const jsonResponse = await response.json();
      const user_id = users[index].user_id;
      await fetch(`http://localhost:${PORT}/chat/users_chats`, {
        method: 'POST',
        credentials: 'include',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          user_id: sessionStorage.getItem('user_id'),
          chat_id: jsonResponse.chat_id,
        })
      })
      await fetch(`http://localhost:${PORT}/chat/users_chats`, {
        method: 'POST',
        credentials: 'include',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          user_id: user_id,
          chat_id: jsonResponse.chat_id,
        })
      })
      setFilteringUsers(false);
    } catch(error) {
      console.log(error);
    }

  }

  return (
    <Autocomplete
      value={value}
      options={arr}
      sx={{ width: '100%', marginTop:'10px'}}
      renderInput={(params) => <TextField {...params} label="Search contact"/>}
      onChange={(e, v) => handleChange(e, v)}
    />
  )
}
