import {useCallback, useMemo, useState, useRef, useEffect} from 'react';
import {Contacts} from './Contacts.js';
import {Chat} from './Chat.js';
import {Navigate, useNavigate} from 'react-router-dom';

export const Dashboard = () => {

  const [contact, setContact] = useState({});
  const [messages, setMessages] = useState([]);
  
  const WEBSOCKET_PORT = process.env.REACT_APP_SERVER_PORT;
  
  const navigate = useNavigate();
  
  const socketRef = useRef(null);
  
  useEffect(() => {
    if (!sessionStorage.getItem("username")) {
      return;
    }
    
    const socket = new WebSocket("ws://localhost:" + WEBSOCKET_PORT + "/ws");
    socketRef.current = socket;
    
    let heartbeat = null;
    socket.onopen = () => {
      const message = {new_client_id: sessionStorage.getItem("user_id")}
      socket.send(JSON.stringify(message));

      heartbeat = setInterval(() => {
        if (socket.readyState === WebSocket.OPEN) {
          socket.send(JSON.stringify({"type": "ping"}));
        }
      }, 10_000);
    }
  
    socket.onmessage = event => {
      const jsonData = JSON.parse(event.data);
      if (jsonData.content) {
        setMessages(prevMessages => [...prevMessages, jsonData]);
      }
    }
  
    socket.onclose = () => {
      if (heartbeat !== null) {
        clearInterval(socket.heartbeat);
        heartbeat = null 
      }
      if (socket.current === socket) {
        socket.current = null;
      }
    }
  
    return () => {
      if (heartbeat !== null) {
        clearInterval(heartbeat);
      }

      // Prevent this cleanup from triggering logout behavior.
      socket.onclose = null;
      socket.onopen = null;
      socket.onmessage = null;
      socket.onerror = null;

      if (socket.readyState === WebSocket.OPEN ||
          socket.readyState === WebSocket.CONNECTING) {
        socket.close();
      }

      if (socketRef.current === socket) {
        socketRef.current = null;
      }
    }
  }, [WEBSOCKET_PORT]);

  const sendMessageToSocket = useCallback(data => {
    const socket = socketRef.current;
    if (socket?.readyState === WebSocket.OPEN) {
      socket.send(data);
    } else {
      console.log("Socket is not connected");
    }
  }, []);

  const closeSocket = useCallback(() => {
    const socket = socketRef.current;
    if (socket) {
      socketRef.current = null;
      socket.close();
    }
    sessionStorage.setItem('user_id', '');
    sessionStorage.setItem('username', '');
    navigate('/');
  }, [navigate]);
  
  if (!sessionStorage.getItem('username')) {
    return (<Navigate to='/'/>);
  }

  return (
    <div style={{display:'flex', width:'100vw', height:'100vh'}}>
      <Contacts setContact={setContact} closeSocket={closeSocket} />
      <Chat contact={contact} sendMessageToSocket={sendMessageToSocket} messages={messages} setMessages={setMessages} />
    </div>
  )
}
