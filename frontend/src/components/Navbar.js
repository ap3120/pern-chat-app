import { AppBar, Toolbar, Tooltip, IconButton, Typography, Box } from "@mui/material"
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import { MoreVert } from "@mui/icons-material";
import {ThemeToggle} from './ThemeToggle';
import ChatBubbleIcon from '@mui/icons-material/ChatBubble';

export const Navbar = ({setFilteringUsers, setUsers}) => {

  const handleClick = async() => {
    setFilteringUsers(true);
    const response = await fetch('http://localhost:3000/users');
    const jsonResponse = await response.json();
    setUsers(jsonResponse);
  }

  return (
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
            <IconButton color='inherit'>
              <MoreVert/>
            </IconButton>
          </Tooltip>
          <ThemeToggle/>
        </Box>
      </Toolbar>
    </AppBar>
  )
}
