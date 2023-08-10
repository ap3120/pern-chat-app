import {useState} from 'react';
import {Contacts} from './Contacts.js';
import {Chat} from './Chat.js';
import {Navigate} from 'react-router-dom';

export const Dashboard = () => {

  const [contact, setContact] = useState({});

  if (!sessionStorage.getItem('username')) {
    return (<Navigate to='/'/>);
  }  

  return (
    <div style={{display:'flex', width:'100vw', height:'100vh'}}>
      <Contacts setContact={setContact}/>
      <Chat contact={contact}/>
    </div>
  )
}
