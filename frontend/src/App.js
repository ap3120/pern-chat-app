// react router
import {BrowserRouter as Router, Switch, Routes, Route, Link, Navigate} from "react-router-dom";

// components
import {RegisterCard} from './components/RegisterCard';
import {ThemeSettingProvider, useTheme} from './context/ThemeContext';
import { ThemeProvider, createTheme } from '@mui/material/styles';



function App() {

  const dark = useTheme();

  const theme = createTheme({
    palette: {
      primary: {
        main: dark ? '#9c27b0' : '#7b1fa2',
        contrastText: '#fff',
      },
      /*secondary: {
      main:,
      contrastText:,
    },
    error: {
      main:,
      contrastText:,
    },
    warning: {
      main:,
      contrastText:,
    },
    info: {
      main:,
      contrastText:,
    },
    success: {
      main:,
      contrastText:,
    },*/
    },
  });

  return (
    <ThemeSettingProvider>
      <ThemeProvider theme={theme}>
        <Router>
          <Routes>
            <Route path='' element={<RegisterCard/>}/>
          </Routes>
        </Router>
      </ThemeProvider>
    </ThemeSettingProvider>
  );
}

export default App;
