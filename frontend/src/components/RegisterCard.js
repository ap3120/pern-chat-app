import {useState} from 'react';
import {Button, Card, TextField, IconButton} from '@mui/material';
import { useTheme } from '../context/ThemeContext.js';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import InputAdornment from '@mui/material/InputAdornment';
import OutlinedInput from '@mui/material/OutlinedInput';
import './RegisterCard.css';

export const RegisterCard = () => {

  const [showPassword, setShowPassword] = useState(false);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const dark = useTheme();

  const ENDPOINT = 'http://localhost/3000';

  const handleClickShowPassword = () => setShowPassword((show) => !show);

  const handleRegister = async() => {
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
    const newUser = await response.json();
    setUsername(newUser);
  }

  useEffect(() => {console.log(username)}, [username])
  return (
    <div style={{width:'100%', height:'100vh', backgroundColor: dark ? '#212121' : '#fff', display:'flex', justifyContent:'center', alignItems:'center'}}>
      <Card sx={{display:'flex', flexDirection:'column', justifyContent:'center', alignItems:'center', p:5}}>
        <TextField label="Username" variant="outlined" sx={{m:1, width:'100%'}} onChange={(e) => {setUsername(e.target.value)}}/>
        <OutlinedInput
          id="outlined-adornment-password"
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
        />
        <Button variant='contained' onClick={handleRegister}>Register</Button>
      </Card>
    </div>
  )
}
