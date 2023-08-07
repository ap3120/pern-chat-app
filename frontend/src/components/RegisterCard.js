import {useState, useEffect} from 'react';
import {Button, Card, TextField, IconButton, FormHelperText, Snackbar, Alert} from '@mui/material';
import { useTheme } from '../context/ThemeContext.js';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import InputAdornment from '@mui/material/InputAdornment';
import OutlinedInput from '@mui/material/OutlinedInput';
import { NavLink, Navigate } from 'react-router-dom';
import {ThemeToggle} from './ThemeToggle.js';
//import {getCurrentSession} from '../utils/getCurrentSession.js';

export const RegisterCard = () => {

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmationPassword, setShowConfirmationPassword] = useState(false);
  const [username, setUsername] = useState('');
  const [newUsername, setNewUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmationPassword, setConfirmationPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [openError, setOpenError] = useState(false);
  const [openSuccess, setOpenSuccess] = useState(false);
  const [msg, setMsg] = useState('');

  const dark = useTheme();

  const ENDPOINT = 'http://localhost:3000';

  const handleClickShowPassword = () => setShowPassword((show) => !show);
  const handleClickShowConfirmationPassword = () => setShowConfirmationPassword((show) => !show);

  const validate = () => {
    let err = {};
    err.username = username === '' ? 'Username is required' : '';
    err.password = password.length < 8 ? 'Password must be at least height characters long' : '';
    err.confirmationPassword = password !== confirmationPassword ? 'Passwords must match' : '';
    setErrors(err);
    return Object.values(err).every(x => x === '');
  }

  const handleRegister = async() => {
    if (!validate()) {return;}
    try {
      const response = await fetch(`${ENDPOINT}/register`, {
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
        setMsg('Something went wrong...');
        setOpenError(true);
        throw new Error("Network response was not OK.");
      }
      const jsonResponse = await response.json();
      if (jsonResponse.msg) {
        setMsg(jsonResponse.msg);
        setOpenError(true);
      } else {
        setNewUsername(jsonResponse.username);
        setOpenSuccess(true);
      }
    } catch (error) {
      console.log(error);
      setMsg("Couldn't connect to server...");
      setOpenError(true);
    }
  }

  const handleCloseSuccess = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }
    setOpenSuccess(false);
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
    <div style={{width:'100%', height:'100vh', backgroundColor: dark ? '#212121' : '#fff', display:'flex', justifyContent:'center', alignItems:'center'}}>
      <Card sx={{display:'flex', flexDirection:'column', justifyContent:'center', alignItems:'center', p:5}}>
        <ThemeToggle />
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
        <OutlinedInput
          type={showConfirmationPassword ? 'text' : 'password'}
          endAdornment={
            <InputAdornment position="end">
              <IconButton
                aria-label="toggle password visibility"
                onClick={handleClickShowConfirmationPassword}
                edge="end"
              >
                {showConfirmationPassword ? <VisibilityOff /> : <Visibility />}
              </IconButton>
            </InputAdornment>
          }
          placeholder="Confirm Password"
          sx={{m:1, width:'100%'}}
          onChange={(e) => {setConfirmationPassword(e.target.value)}}
          {...(errors.confirmationPassword && {error: true})}
        />
        {errors.confirmationPassword && (
          <FormHelperText error>{errors.confirmationPassword}</FormHelperText>
        )}
        <Button variant='contained' onClick={handleRegister}>Register</Button>
        <p>Already have an account ? <NavLink to='/'>Login here</NavLink></p>
      </Card>
      <Snackbar open={openSuccess} autoHideDuration={3000} onClose={handleCloseSuccess} anchorOrigin={{vertical: 'bottom', horizontal: 'center'}}>
        <Alert severity='success' onClose={handleCloseSuccess}>User {newUsername} was successfully created.</Alert>
      </Snackbar>
      <Snackbar open={openError} autoHideDuration={3000} onClose={handleCloseError} anchorOrigin={{vertical: 'bottom', horizontal: 'center'}}>
        <Alert severity='error' onClose={handleCloseError}>{msg}</Alert>
      </Snackbar>
    </div>
  )
}
