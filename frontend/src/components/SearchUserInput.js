import {TextField, Autocomplete, Alert, Snackbar} from '@mui/material';
import {useState} from 'react';
export const SearchUserInput = ({users, setChats, setFilteringUsers}) => {

  const PORT = process.env.REACT_APP_SERVER_PORT;
  const [value, setValue] = useState('');
  const [error, setError] = useState("");
  const [openError, setOpenError] = useState(false);

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
          created_by: sessionStorage.getItem('user_id'),
          other_user_id: users[index].user_id
        })
      })
      const jsonResponse = await response.json();
      if (!response.ok) {
        setError(jsonResponse.error ? jsonResponse.error : "Something went wrong...")
        setOpenError(true);
      }
      setFilteringUsers(false);
    } catch(error) {
      console.log(error);
    }

  }

  const handleCloseError = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }
    setOpenError(false);
  }

  return (
    <>
      <Autocomplete
      value={value}
      options={arr}
      sx={{ width: '100%', marginTop:'10px'}}
      renderInput={(params) => <TextField {...params} label="Search contact"/>}
      onChange={(e, v) => handleChange(e, v)}
      />
      <Snackbar open={openError} autoHideDuration={3000} onClose={handleCloseError} anchorOrigin={{vertical: 'bottom', horizontal: 'center'}}>
        <Alert data-testid="error-alert" severity='error' onClose={handleCloseError}>{error}</Alert>
      </Snackbar>
    </>
  )
}
