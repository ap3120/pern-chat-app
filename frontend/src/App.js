// react router
import {HashRouter as Router, Routes, Route} from "react-router-dom";

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
        main: dark ? '#892b59' : '#802957',
        contrastText: '#cfdeda',
      },
      secondary: {
        main: dark ? '#ca493c': '#b13625',
        contrastText: '#cfdeda',
      },
      background: {
        main: dark ? '#272727' : '#fff',
        default: dark ? '#272727' : '#fff',
        paper: dark ? '#272727' : '#fff',
        hover: dark ? '#515151' : '#b9b9b9',
      },
      text: {
        main: dark ? '#cfdeda' : '#000',
        primary: dark ? '#cfdeda' : '#000',
        secondary: dark ? '#cfdeda' : '#000',
      },
      action: {
        hover: dark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.04)',
      }
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
