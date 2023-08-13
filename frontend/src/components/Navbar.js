import { useState } from "react";
import { AppBar, Toolbar, Tooltip, IconButton, Typography, Box, Menu, MenuItem } from "@mui/material"
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import { MoreVert } from "@mui/icons-material";
import {ThemeToggle} from './ThemeToggle';
import ChatBubbleIcon from '@mui/icons-material/ChatBubble';
import {useNavigate} from 'react-router-dom';

export const Navbar = ({setFilteringUsers, setUsers}) => {

  const [anchorEl, setAnchorEl] = useState(null);
  const open = Boolean(anchorEl);

  const navigate = useNavigate();

  const handleClick = async() => {
    setFilteringUsers(true);
    const response = await fetch('http://localhost:3000/users');
    const jsonResponse = await response.json();
    setUsers(jsonResponse);
  }

  const openSettings = e => {
    setAnchorEl(e.currentTarget);
  }

  const handleClose = () => {
    setAnchorEl(null);
  }

  const handleLogout = async() => {
    setAnchorEl(null);
    try {
      const response = await fetch('http://localhost:3000/logout');
      const jsonResponse = await response.json();
      if (jsonResponse.msg === "Successfully logged out.") {
        sessionStorage.setItem('user_id', '');
        sessionStorage.setItem('username', '');
        navigate('/');
      }
    } catch(error) {
      console.log(error);
    }
  }

  const handleProfile = () => {
    setAnchorEl(null);
    navigate('/profile');
  }

  return (
    <>
      <AppBar position='static'>
        <Toolbar sx={{display:'flex', justifyContent:'space-between'}}>
          <Box sx={{display:'flex', alignItems:'center'}}>
            <AccountCircleIcon sx={{fontSize:'50px', marginRight:'20px'}}/>
            <Typography>{sessionStorage.getItem('username')}</Typography>
          </Box>
          <Box>
            <Tooltip title='New chat'>
              <IconButton color='inherit' onClick={handleClick}>
                <ChatBubbleIcon/>
              </IconButton>
            </Tooltip>
            <Tooltip title="Settings">
              <IconButton color='inherit' onClick={(e) => openSettings(e)}>
                <MoreVert/>
              </IconButton>
            </Tooltip>
            <ThemeToggle/>
          </Box>
        </Toolbar>
      </AppBar>
      <Menu
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        MenuListProps={{
          'aria-labelledby': 'basic-button',
        }}
      >
        <MenuItem onClick={handleProfile}>Profile</MenuItem>
        <MenuItem onClick={handleLogout}>Logout</MenuItem>
      </Menu>
    </>
  )
}
