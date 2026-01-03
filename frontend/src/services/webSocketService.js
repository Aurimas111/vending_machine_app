import { Client } from '@stomp/stompjs';

const socket = new Client({
  brokerURL: 'ws://localhost:8080/ws',
  reconnectDelay: 5000,
  debug: (str) => console.log('STOMP:', str),
});

socket.beforeConnect = () => {
  const token = localStorage.getItem('jwt');
  if (token) {
    socket.connectHeaders = {
      Authorization: `Bearer ${token}`,
    };
  }
};

export default socket;