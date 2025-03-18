import {useCallback, useState} from 'react';
import {Contacts} from './Contacts.js';
import {Chat} from './Chat.js';
import {Navigate} from 'react-router-dom';

export const Dashboard = () => {

  const [contact, setContact] = useState({});
  const [messages, setMessages] = useState([]);

  const socket = new WebSocket("ws://localhost:8080/ws");
  
  const sendMessageToSocket = useCallback(data => {
    if (socket) {
      socket.send(data);
    }
  }, [socket]);
  
  if (!sessionStorage.getItem('username')) {
    return (<Navigate to='/'/>);
  }

  socket.onopen = () => {
    const message = {new_client_id: sessionStorage.getItem("user_id")}
    socket.send(JSON.stringify(message));

  }

  socket.onmessage = event => {
    console.log(event.data);
    const jsonData = JSON.parse(event.data);
    if (jsonData.content) setMessages(prevMessages => [...prevMessages, jsonData]);
  }

  socket.onclose = () => {
    const message = {client_id_to_remove: sessionStorage.getItem("user_id")};
    socket.send(JSON.stringify(message));
    sessionStorage.setItem('user_id', '');
    sessionStorage.setItem('username', '');
    return (<Navigate to="/" />);

  }

  return (
    <div style={{display:'flex', width:'100vw', height:'100vh'}}>
      <Contacts setContact={setContact}/>
      <Chat contact={contact} sendMessageToSocket={sendMessageToSocket} messages={messages} setMessages={setMessages} />
    </div>
  )
}
