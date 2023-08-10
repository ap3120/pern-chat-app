import { useEffect } from "react";

export const MessageContainer = ({contact, messages, setMessages}) => {

  const getMessages = async() => {
    try {
      console.log(contact.chat_id);
      const response = await fetch(`http://localhost:3000/message/${contact.chat_id}`);
      const jsonResponse = await response.json();
      console.log(jsonResponse)
      setMessages(jsonResponse);
    } catch (error) {
      console.log(error);
    }
  }

  useEffect(() => {
    getMessages();
  }, [contact])

  return (

    <div style={{width:'100%', flexGrow:1, border:'1px solid green'}}>
      {messages.map((msg, index) => (
        <div key={index}>{msg.content}</div>
      ))}
    </div>
  )
}
