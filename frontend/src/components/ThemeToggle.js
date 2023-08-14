import Switch from "@mui/material/Switch";
import {useTheme, useThemeUpdate} from '../context/ThemeContext.js';
import { styled } from "@mui/system";
import nightIcon from '../media/icons8-night-48.png';
import sunIcon from '../media/icons8-sun-48.png';

const StyledSwitch = styled(Switch)(({ theme }) => ({
  width: 62,
  height: 34,
  padding: 7,
  '& .MuiSwitch-switchBase': {
    margin: 1,
    padding: 0,
    transform: 'translateX(6px)',
    '&.Mui-checked': {
      color: '#fff',
      transform: 'translateX(22px)',
      '& .MuiSwitch-thumb:before': {
        backgroundImage: `url(${nightIcon})`,
        backgroundSize: '20px 20px',
      },
      '& + .MuiSwitch-track': {
        opacity: 1,
        backgroundColor: '#aab4be',
      },
    },
  },
  '& .MuiSwitch-thumb': {
    backgroundColor: '#001e3c',
    width: 32,
    height: 32,
    '&:before': {
      content: "''",
      position: 'absolute',
      width: '100%',
      height: '100%',
      left: 0,
      top: 0,
      backgroundRepeat: 'no-repeat',
      backgroundPosition: 'center',
      backgroundImage: `url(${sunIcon})`,
      backgroundSize: '20px 20px',
    },
  },
  '& .MuiSwitch-track': {
    opacity: 1,
    backgroundColor: '#aab4be',
    borderRadius: 20 / 2,
  },
}));

export const ThemeToggle = () => {
  const theme = useTheme()
  const themeSwitch = useThemeUpdate();

  const handleChange = () => {
    themeSwitch();
  }

  return (
    <StyledSwitch checked={theme} onChange={handleChange}/>
  )
}
