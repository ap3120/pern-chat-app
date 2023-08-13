import { useState } from "react";
import {Button, Card, IconButton, FormHelperText, Snackbar, Alert, Typography} from '@mui/material';
import { useTheme } from '../context/ThemeContext.js';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import InputAdornment from '@mui/material/InputAdornment';
import OutlinedInput from '@mui/material/OutlinedInput';
import {ThemeToggle} from './ThemeToggle.js';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useNavigate } from "react-router-dom";

export const Profile = () => {
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [openError, setOpenError] = useState(false);
  const [openSuccess, setOpenSuccess] = useState(false);
  const [msg, setMsg] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState({});

  const dark = useTheme();

  const navigate = useNavigate();
  
  const validate = () => {
    let tmp = {};
    tmp.currentPassword = currentPassword === '' ? 'This field is required' : '';
    tmp.password = password.length < 8 ? 'Password must be at least eight characters long' : '';
    tmp.confirmPassword = password !== confirmPassword ? 'Passwords do not match' : '';
    setErrors(tmp);
    return Object.values(tmp).every(x => x === '');
  }

  const handleClickShowCurrentPassword = () => {
    setShowCurrentPassword(prevState => !prevState);
  }
  const handleClickShowPassword = () => {
    setShowPassword(prevState => !prevState);
  }
  const handleClickShowConfirmPassword = () => {
    setShowConfirmPassword(prevState => !prevState);
  }

  const handleCloseError = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }
    setOpenError(false);
  }

  const handleCloseSuccess = (event, reason) => {
    if (reason === 'clickaway') {
      return;
    }
    setOpenSuccess(false);
  }

  const handleClick = async() => {
    if (!validate()) return;
    try {
      const response = await fetch(`http://localhost:3000/users/${sessionStorage.getItem('user_id')}`, {
        method: 'PUT',
        body: JSON.stringify({
          currentPassword: currentPassword,
          password: password,
        }),
        headers: {'Content-Type': 'application/json'}
      })
      const jsonResponse = await response.json();
      if (jsonResponse.msg === 'Password successfully updated.') {
        setMsg('Your password was successfully updated!');
        setOpenSuccess(true);
      } else {
        setMsg("Incorrect password...")
        setOpenError(true);
      }
    } catch(error) {
      console.log(error)
      setMsg("Couldn't connect to server...");
      setOpenError(true);
    }
  }

  return (
    <div style={{width:'100%', height:'100vh', backgroundColor: dark ? '#212121' : '#fff', display:'flex', justifyContent:'space-around', alignItems:'center', flexDirection:'column'}}>
      <div style={{display:'flex'}}>
        <IconButton onClick={() => {navigate('/dashboard')}}>
          <ArrowBackIcon/>
        </IconButton>
        <Typography variant='h2'>Welcome, {sessionStorage.getItem('username')}</Typography>
      </div>
      <Card sx={{display:'flex', flexDirection:'column', justifyContent:'center', alignItems:'center', p:5}}>
        <ThemeToggle/>
        <OutlinedInput
          type={showCurrentPassword ? 'text' : 'password'}
          endAdornment={
            <InputAdornment position="end">
              <IconButton
                aria-label="toggle password visibility"
                onClick={handleClickShowCurrentPassword}
                edge="end"
              >
                {showCurrentPassword ? <VisibilityOff /> : <Visibility />}
              </IconButton>
            </InputAdornment>
          }
          placeholder="Current Password"
          sx={{m:1, width:'100%'}}
          onChange={(e) => {setCurrentPassword(e.target.value)}}
          {...(errors.currentPassword && {error: true})}
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
        <OutlinedInput
          type={showConfirmPassword ? 'text' : 'password'}
          endAdornment={
            <InputAdornment position="end">
              <IconButton
                aria-label="toggle password visibility"
                onClick={handleClickShowConfirmPassword}
                edge="end"
              >
                {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
              </IconButton>
            </InputAdornment>
          }
          placeholder="Confirm Password"
          sx={{m:1, width:'100%'}}
          onChange={(e) => {setConfirmPassword(e.target.value)}}
          {...(errors.confirmPassword && {error: true})}
        />
        {errors.currentPassword && (
          <FormHelperText error>{errors.currentPassword}</FormHelperText>
        )}
        {errors.password && (
          <FormHelperText error>{errors.password}</FormHelperText>
        )}
        {errors.confirmPassword && (
          <FormHelperText error>{errors.confirmPassword}</FormHelperText>
        )}
        <Button variant='contained' onClick={handleClick}>Change Password</Button>
      </Card>
      <Snackbar open={openError} autoHideDuration={3000} onClose={handleCloseError} anchorOrigin={{vertical: 'bottom', horizontal: 'center'}}>
        <Alert severity='error' onClose={handleCloseError}>{msg}</Alert>
      </Snackbar>
      <Snackbar open={openSuccess} autoHideDuration={3000} onClose={handleCloseSuccess} anchorOrigin={{vertical: 'bottom', horizontal: 'center'}}>
        <Alert severity='success' onClose={handleCloseError}>{msg}</Alert>
      </Snackbar>
    </div>
  )
}
