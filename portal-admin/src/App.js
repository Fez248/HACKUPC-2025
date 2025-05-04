import React, { useState } from 'react';
import './App.css';

function App() {
  // Estado para controlar si el usuario está "logueado"
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  
  // Define la URL base de la API
  const API_BASE_URL = "http://10.0.0.1";

  // Estado para la columna Data
  const [data, setData] = useState({
    flightNumber: '',
    cityOriginFull: '',
    cityOriginCode: '',
    departureTime: '',
    cityDestFull: '',
    cityDestCode: '',
    landingTime: '',
    status: ''
  });

  // Estado para la columna News
  const [news, setNews] = useState({
    id: '',
    flightNumber: '',
    title: '',
    content: ''
  });

  // Manejadores genéricos para actualizar los estados
  const handleDataChange = (e) => {
    setData({ ...data, [e.target.name]: e.target.value });
  };

  const handleNewsChange = (e) => {
    setNews({ ...news, [e.target.name]: e.target.value });
  };

  // Función para iniciar sesión
  const handleLogin = () => {
    setIsLoggedIn(true);
  };

  // Función para cerrar sesión
  const handleLogout = () => {
    setIsLoggedIn(false);
  };

  // Función para enviar los datos de vuelo mediante PATCH a /api/admin/:flightNumber/data
  const sendData = async () => {
    if (!data.flightNumber) {
      console.error("El número de vuelo es obligatorio para actualizar los datos.");
      return;
    }

    // Obtener la hora actual en formato HH:MM
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const timeFormat = `${hours}:${minutes}`;

    const updateBody = {
      originFull: data.cityOriginFull,
      originShort: data.cityOriginCode,
      departureTime: data.departureTime,
      destinationFull: data.cityDestFull,
      destinationShort: data.cityDestCode,
      landingTime: data.landingTime,
      status: data.status,
      date: timeFormat // Cambiado de formato ISO a HH:MM
    };

    try {
      const response = await fetch(`${API_BASE_URL}/api/admin/${data.flightNumber}/data`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updateBody)
      });

      const result = await response.json();
      console.log("Resultado de actualización:", result);
    } catch (error) {
      console.error("Error actualizando datos del vuelo:", error);
    }
  };

  // Función para enviar una noticia mediante POST a /api/admin/news
  const sendNews = async () => {
    if (!news.flightNumber || !news.id) {
      console.error("El número de vuelo y el ID de la noticia son obligatorios.");
      return;
    }

    // Obtener la hora actual en formato HH:MM
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const timeFormat = `${hours}:${minutes}`;

    const newsPayload = {
      id: news.id,
      flightNumber: news.flightNumber,
      title: news.title,
      content: news.content,
      date: timeFormat // Cambiado de formato ISO a HH:MM
    };

    try {
      const response = await fetch(`${API_BASE_URL}/api/admin/news`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newsPayload)
      });
      const result = await response.json();
      console.log("Resultado del envío de la noticia:", result);
    } catch (error) {
      console.error("Error enviando la noticia:", error);
    }
  };

  // Si el usuario no está logueado, mostrar pantalla de login
  if (!isLoggedIn) {
    return (
      <div className="login-container" style={{ 
        maxWidth: '400px', 
        margin: '100px auto', 
        padding: '20px',
        boxShadow: '0 0 10px rgba(0,0,0,0.1)',
        borderRadius: '5px'
      }}>
        <h1>Admin Portal Vueling</h1>
        <h2>Login</h2>
        <form style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
          <div>
            <label htmlFor="username">Username:</label>
            <input
              type="text"
              id="username"
              placeholder="Username"
              style={{ width: '100%', padding: '8px', marginTop: '5px' }}
            />
          </div>
          <div>
            <label htmlFor="password">Password:</label>
            <input
              type="password"
              id="password"
              placeholder="Password"
              style={{ width: '100%', padding: '8px', marginTop: '5px' }}
            />
          </div>
          <button 
            type="button"
            onClick={handleLogin}
            style={{ 
              padding: '10px', 
              backgroundColor: '#0056b3', 
              color: 'white', 
              border: 'none', 
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Login
          </button>
        </form>
      </div>
    );
  }

  // Si el usuario está logueado, mostrar la aplicación principal
  return (
    <div className="App">
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0 20px' }}>
        <h1 style={{ textAlign: 'left' }}>Admin Portal Vueling</h1>
        <button 
          onClick={handleLogout} 
          style={{ 
            padding: '8px 16px', 
            backgroundColor: '#dc3545', 
            color: 'white', 
            border: 'none', 
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Logout
        </button>
      </header>
      <div style={{ display: 'flex', justifyContent: 'space-between', padding: '20px', gap: '40px' }}>
        {/* Columna Data */}
        <div style={{ flex: 1 }}>
          <h2 style={{ textAlign: 'left' }}>Data</h2>
          <form style={{ textAlign: 'left' }}>
            <div style={{ marginBottom: '10px' }}>
              <label>Flight number: </label>
              <input
                type="text"
                name="flightNumber"
                placeholder="Enter flight number"
                value={data.flightNumber}
                onChange={handleDataChange}
              />
            </div>
            <div style={{ marginBottom: '10px' }}>
              <label>City of origin (full): </label>
              <input
                type="text"
                name="cityOriginFull"
                placeholder="Enter city of origin (full)"
                value={data.cityOriginFull}
                onChange={handleDataChange}
              />
            </div>
            <div style={{ marginBottom: '10px' }}>
              <label>City of origin (code): </label>
              <input
                type="text"
                name="cityOriginCode"
                placeholder="Enter city of origin (code)"
                value={data.cityOriginCode}
                onChange={handleDataChange}
              />
            </div>
            <div style={{ marginBottom: '10px' }}>
              <label>Departure time: </label>
              <input
                type="text"
                name="departureTime"
                placeholder="Enter departure time"
                value={data.departureTime}
                onChange={handleDataChange}
              />
            </div>
            <div style={{ marginBottom: '10px' }}>
              <label>City of destination (full): </label>
              <input
                type="text"
                name="cityDestFull"
                placeholder="Enter city of destination (full)"
                value={data.cityDestFull}
                onChange={handleDataChange}
              />
            </div>
            <div style={{ marginBottom: '10px' }}>
              <label>City of destination (code): </label>
              <input
                type="text"
                name="cityDestCode"
                placeholder="Enter city of destination (code)"
                value={data.cityDestCode}
                onChange={handleDataChange}
              />
            </div>
            <div style={{ marginBottom: '10px' }}>
              <label>Landing time: </label>
              <input
                type="text"
                name="landingTime"
                placeholder="Enter landing time"
                value={data.landingTime}
                onChange={handleDataChange}
              />
            </div>
            <div style={{ marginBottom: '10px' }}>
              <label>Status: </label>
              <input
                type="text"
                name="status"
                placeholder="Enter status"
                value={data.status}
                onChange={handleDataChange}
              />
            </div>
            <button type="button" onClick={sendData}>Send</button>
          </form>
        </div>

        {/* Columna News */}
        <div style={{ flex: 1 }}>
          <h2 style={{ textAlign: 'left' }}>News</h2>
          <form style={{ textAlign: 'left' }}>
            <div style={{ marginBottom: '10px' }}>
              <label>Id: </label>
              <input
                type="text"
                name="id"
                placeholder="Enter id"
                value={news.id}
                onChange={handleNewsChange}
              />
            </div>
            <div style={{ marginBottom: '10px' }}>
              <label>Flight number: </label>
              <input
                type="text"
                name="flightNumber"
                placeholder="Enter flight number"
                value={news.flightNumber}
                onChange={handleNewsChange}
              />
            </div>
            <div style={{ marginBottom: '10px' }}>
              <label>Title: </label>
              <input
                type="text"
                name="title"
                placeholder="Enter title"
                value={news.title}
                onChange={handleNewsChange}
              />
            </div>
            <div style={{ marginBottom: '10px' }}>
              <label>Content: </label>
              <input
                type="text"
                name="content"
                placeholder="Enter content"
                value={news.content}
                onChange={handleNewsChange}
              />
            </div>
            <button type="button" onClick={sendNews}>Send</button>
          </form>
        </div>
      </div>
    </div>
  );
}

export default App;