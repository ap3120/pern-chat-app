// react router
import {BrowserRouter as Router, Switch, Routes, Route, Link, Navigate} from "react-router-dom";

// components
import {RegisterCard} from './components/RegisterCard';
import {LoginCard} from './components/LoginCard.js';
import { Dashboard } from "./components/Dashboard.js";
import {Profile} from './components/profile';

import {ThemeSettingProvider, useTheme} from './context/ThemeContext';
import { ThemeProvider, createTheme } from '@mui/material/styles';

export const App = () => {
  return (
    <ThemeSettingProvider>
      <AppContent />
    </ThemeSettingProvider>
  )
}

const AppContent = () => {

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
    <ThemeProvider theme={theme}>
      <Router>
        <Routes>
          <Route path='/' element={<LoginCard/>}/>
          <Route path='/register' element={<RegisterCard/>}/>
          <Route path='/dashboard' element={<Dashboard/>}/>
          <Route path='/profile' element={<Profile/>}/>
        </Routes>
      </Router>
    </ThemeProvider>
  );
}
