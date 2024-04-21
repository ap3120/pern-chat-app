import { TextField, Autocomplete } from '@mui/material';
import {useState} from 'react';
export const SearchUserInput = ({users, setChats, setFilteringUsers}) => {

  const [value, setValue] = useState('');
  const PORT = process.env.REACT_APP_PORT;

  const arr = users.map(elem => elem.username);

  const handleChange = async(e, v) => {
    const index = users.findIndex(elem => elem.username === v);
    if (index === -1) {return;}
    setValue(v);
    try {
    const response = await fetch(`http://localhost:${PORT}/chat`, {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({
        created_by: sessionStorage.getItem('user_id'),
        created_at: new Date(),
      })
    })
    const jsonResponse = await response.json();
      const user_id = users[index].user_id;
      const resp = await fetch(`http://localhost:${PORT}/chat/users_chats`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          user_id: user_id,
          chat_id: jsonResponse.chat_id,
        })
      })
      const jsonResp = await resp.json();
      setChats(prevChats => [...prevChats, jsonResp]);
      setFilteringUsers(false);
    } catch(error) {
      console.log(error);
    }

  }

  return (
    <Autocomplete
      value={value}
      //disablePortal
      options={arr}
      sx={{ width: '100%', marginTop:'10px'}}
      renderInput={(params) => <TextField {...params} label="Search contact"/>}
      onChange={(e, v) => handleChange(e, v)}
    />
  )
}
