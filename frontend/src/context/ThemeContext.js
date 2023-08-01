import React, { useContext, useState } from "react";

const ThemeContext = React.createContext();
const ThemeUpdateContext = React.createContext();

export function useTheme() {
  return useContext(ThemeContext);
}

export function useThemeUpdate() {
  return useContext(ThemeUpdateContext);
}

export const ThemeSettingProvider = ({ children }) => {
  const [dark, setDark] = useState(true);

  const toggleTheme = () => {
    setDark((prevDark) => !prevDark);
  };

  return (
    <ThemeContext.Provider value={dark}>
      <ThemeUpdateContext.Provider value={toggleTheme}>
        {children}
      </ThemeUpdateContext.Provider>
    </ThemeContext.Provider>
  );
};
