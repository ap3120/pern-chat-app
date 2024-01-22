import {useState} from 'react';
import {Box, Button, Card, TextField, IconButton, FormHelperText, Snackbar, Alert} from '@mui/material';
import { useTheme } from '../context/ThemeContext.js';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import InputAdornment from '@mui/material/InputAdornment';
import OutlinedInput from '@mui/material/OutlinedInput';
import { NavLink, Navigate, useNavigate } from 'react-router-dom';
import {ThemeToggle} from './ThemeToggle.js';
import lightBg from '../media/lightbg.jpeg';
import darkBg from '../media/darkbg.jpeg';

export const LoginCard = () => {

  const [showPassword, setShowPassword] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [openError, setOpenError] = useState(false);
  const [msg, setMsg] = useState('');

  const dark = useTheme();
  const navigate = useNavigate();

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
      if (!response.ok) {
        setErrors({username: 'Invalid credentials', password: 'Invalid credentials'});
        return;
      }
      const jsonResponse = await response.json();
      console.log(jsonResponse)
      sessionStorage.setItem('user_id', jsonResponse.user.user_id);
      sessionStorage.setItem('username', jsonResponse.user.username)
      navigate('/dashboard');
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

  if (sessionStorage.getItem('user_id')) {
    return (<Navigate to='/dashboard'/>);
  }

  return (
    <Box sx={{
      width:'100vw',
      height:'100vh',
      display:'flex',
      justifyContent:'center',
      alignItems:'center',
      backgroundImage: dark ? `url(${darkBg})` : `url(${lightBg})`,
      backgroundRepeat:'no-repeat',
      backgroundSize:'cover',
      backgroundPosition:'center'
    }}>
      <Card sx={{display:'flex', flexDirection:'column', justifyContent:'center', alignItems:'center', p:5, backgroundColor:'transparent', backdropFilter:'blur(20px)'}}>
        <ThemeToggle/>
        <TextField
          data-cy={`username`}
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
    </Box>
  )
}
