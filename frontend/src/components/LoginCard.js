import {useState, useEffect} from 'react';
import {Button, Card, TextField, IconButton, FormHelperText, Snackbar, Alert} from '@mui/material';
import { useTheme } from '../context/ThemeContext.js';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import InputAdornment from '@mui/material/InputAdornment';
import OutlinedInput from '@mui/material/OutlinedInput';
import { NavLink } from 'react-router-dom';
import './RegisterCard.css';

export const LoginCard = () => {

  const [showPassword, setShowPassword] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [openError, setOpenError] = useState(false);
  const [msg, setMsg] = useState('');

  const dark = useTheme();

  const ENDPOINT = 'http://localhost:3000';

  const handleClickShowPassword = () => setShowPassword((show) => !show);

  const validate = () => {
    let err = {};
    err.username = username === '' ? 'Username is required' : '';
    err.password = password === '' ? 'Password is required' : '';
    setErrors(err);
    return Object.values(err).every(x => x === '');
  }

  const handleLogin = async() => {
    if (!validate()) {return;}
    try {
      const response = await fetch(`${ENDPOINT}/login`, {
        method: 'POST',
        body: JSON.stringify({
          username: username,
          password: password
        }),
        headers: {
          "Content-Type": "application/json"
        }
      });
      console.log(response);
      if (!response.ok) {
        setMsg('Something went wrong...');
        setOpenError(true);
        throw new Error("Network response was not OK.");
      }
      const jsonResponse = await response.json();
      console.log(jsonResponse); 
    } catch (error) {
      console.log(error);
      setMsg("Couldn't connect to server...");
      setOpenError(true);
    }
  }

  const handleCloseError = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }
    setOpenError(false);
  }

  return (
    <div style={{width:'100%', height:'100vh', backgroundColor: dark ? '#212121' : '#fff', display:'flex', justifyContent:'center', alignItems:'center'}}>
      <Card sx={{display:'flex', flexDirection:'column', justifyContent:'center', alignItems:'center', p:5}}>
        <TextField
          label="Username"
          variant="outlined"
          sx={{m:1, width:'100%'}}
          onChange={(e) => {setUsername(e.target.value)}}
          {...(errors.username && {error: true, helperText: errors.username})}
        />
        <OutlinedInput
          type={showPassword ? 'text' : 'password'}
          endAdornment={
            <InputAdornment position="end">
              <IconButton
                aria-label="toggle password visibility"
                onClick={handleClickShowPassword}
                edge="end"
              >
                {showPassword ? <VisibilityOff /> : <Visibility />}
              </IconButton>
            </InputAdornment>
          }
          placeholder="Password"
          sx={{m:1, width:'100%'}}
          onChange={(e) => {setPassword(e.target.value)}}
          {...(errors.password && {error: true})}
        />
        {errors.password && (
          <FormHelperText error>{errors.password}</FormHelperText>
        )}
        <Button variant='contained' onClick={handleLogin}>Login</Button>
        <p>Don't have an account yet? <NavLink to='/register'>Register here</NavLink></p>
      </Card>
      <Snackbar open={openError} autoHideDuration={3000} onClose={handleCloseError} anchorOrigin={{vertical: 'bottom', horizontal: 'center'}}>
        <Alert severity='error' onClose={handleCloseError}>{msg}</Alert>
      </Snackbar>
    </div>
  )
}
