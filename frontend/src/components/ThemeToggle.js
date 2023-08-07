import Switch from "@mui/material/Switch";
import {useTheme, useThemeUpdate} from '../context/ThemeContext.js';

export const ThemeToggle = () => {
  const theme = useTheme()
  const themeSwitch = useThemeUpdate();

  const handleChange = () => {
    themeSwitch();
  }

  return (
    <Switch color='warning' checked={theme} onChange={handleChange}/>
  )
}
