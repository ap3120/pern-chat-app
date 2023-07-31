// react router
import {BrowserRouter as Router, Switch, Routes, Route, Link, Navigate} from "react-router-dom";

// components
import {RegisterCard} from './components/RegisterCard';

function App() {
  return (
    <Router>
      <Routes>
        <Route path='' element={<RegisterCard/>}/>
      </Routes>
    </Router>
  );
}

export default App;
