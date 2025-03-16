import {useState} from 'react';
import {Contacts} from './Contacts.js';
import {Chat} from './Chat.js';
import {Navigate} from 'react-router-dom';

export const Dashboard = () => {

  const [contact, setContact] = useState({});

  if (!sessionStorage.getItem('username')) {
    return (<Navigate to='/'/>);
  }

  const socket = new WebSocket("ws://localhost:8080/ws");

  socket.onopen = event => {
    socket.send(sessionStorage.getItem("user_id"));

  }

  socket.onmessage = event => {
    console.log(event.data);
  }

  socket.onclose = event => {
    console.log("Closing websocket");
    sessionStorage.setItem('user_id', '');
    sessionStorage.setItem('username', '');
    return (<Navigate to="/" />);

  }

  return (
    <div style={{display:'flex', width:'100vw', height:'100vh'}}>
      <Contacts setContact={setContact}/>
      <Chat contact={contact}/>
    </div>
  )
}
