import {useCallback, useState, useMemo} from 'react';
import {Contacts} from './Contacts.js';
import {Chat} from './Chat.js';
import {Navigate, useNavigate} from 'react-router-dom';

export const Dashboard = () => {

  const [contact, setContact] = useState({});
  const [messages, setMessages] = useState([]);
  
  const navigate = useNavigate();

  const socket = useMemo(() => new WebSocket("ws://localhost:8080/ws"), []);
  
  const sendMessageToSocket = useCallback(data => {
    if (socket) {
      socket.send(data);
    }
  }, [socket]);

  const closeSocket = useCallback(() => {
    if (socket) {
      socket.close();
      sessionStorage.setItem('user_id', '');
      sessionStorage.setItem('username', '');
      navigate('/');
    }
  }, [socket, navigate]);
  
  if (!sessionStorage.getItem('username')) {
    return (<Navigate to='/'/>);
  }

  socket.onopen = () => {
    const message = {new_client_id: sessionStorage.getItem("user_id")}
    socket.send(JSON.stringify(message));

  }

  socket.onmessage = event => {
    const jsonData = JSON.parse(event.data);
    if (jsonData.content && (parseInt(jsonData.sender_id) === contact.user_id || jsonData.sender_id === sessionStorage.getItem("user_id"))) {
      setMessages(prevMessages => [...prevMessages, jsonData]);
    }
  }

  socket.onclose = () => {
    sessionStorage.setItem('user_id', '');
    sessionStorage.setItem('username', '');
    navigate("/");
  }

  return (
    <div style={{display:'flex', width:'100vw', height:'100vh'}}>
      <Contacts setContact={setContact} closeSocket={closeSocket} />
      <Chat contact={contact} sendMessageToSocket={sendMessageToSocket} messages={messages} setMessages={setMessages} />
    </div>
  )
}
