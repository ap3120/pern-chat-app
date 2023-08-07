import {Contacts} from './Contacts.js';
import {Chat} from './Chat.js';
import {Navigate} from 'react-router-dom';
import {getCurrentSession} from '../utils/getCurrentSession.js';

export const Dashboard = () => {

  if (!sessionStorage.getItem('username')) {
    return (<Navigate to='/'/>);
  }  

  return (
    <div style={{display:'flex', width:'100vw', height:'100vh'}}>
      <Contacts />
      <Chat />
    </div>
  )
}
