const express = require('express');
const fs = require('fs');

const app = express();

app.use(express.json());

const data = JSON.parse(fs.readFileSync('data.json', 'utf-8'));
const news = JSON.parse(fs.readFileSync('news.json', 'utf-8'));

// Route 1: GET /api/:flightNumber/data
app.get('/api/:flightNumber/data', (req, res) => {
    const { flightNumber } = req.params;

    //Check for flight number
    if (!flightNumber) {
        return res.status(400).json({ error: 'Flight number is required' });
    }

    //If flight number is 'ALL', return all flights
    if (flightNumber === 'ALL') {
        return res.json(data);
    }

    //If flight number is not 'ALL', return specific flights
    const flight = data.find(f => f.flightNumber === flightNumber);
    if (!flight) return res.status(404).json({ error: 'Flight not found' });

    res.json({ data: flight });
});

// Route 2: GET /api/:flightNumber/news
app.get('/api/:flightNumber/news', (req, res) => {
    const { flightNumber } = req.params;

    //Check for flight number
    if (!flightNumber) {
        return res.status(400).json({ error: 'Flight number is required' });
    }

    //If flight number is 'ALL', return all news
    if (flightNumber === 'ALL') {
        return res.json(news);
    }

    //If flight number is not 'ALL', return specific news
    const flightNews = news.filter(n => n.flightNumber === flightNumber);
    if (!flightNews) return res.status(404).json({ error: 'Flight news not found' });

    res.json({ news: flightNews });
});

app.listen(80, () => {
  console.log('HTTP server running on port 80');
});

function saveFlights() {
    fs.writeFileSync('data.json', JSON.stringify(flights, null, 2), 'utf8');
}

// PATCH endpoint to modify flight details
app.patch('/api/admin/flights/:flightNumber', (req, res) => {
    const { flightNumber } = req.params;
    const updates = req.body;

    // Find flight
    const flight = flights.find(f => f.flightNumber === flightNumber);
    if (!flight) {
      return res.status(404).json({ error: 'Flight not found' });
    }

    // Prevent changing flightNumber
    if ('flightNumber' in updates) {
      return res.status(400).json({ error: 'Flight number cannot be modified' });
    }

    // Apply updates to allowed fields
    const allowedFields = ['origin', 'destination', 'departureTime', 'arrivalTime', 'status', 'gate', 'door', 'terminal'];
    for (const key of Object.keys(updates)) {
      if (allowedFields.includes(key)) {
        flight[key] = updates[key];
      } else {
        return res.status(400).json({ error: `Invalid field: ${key}` });
      }
    }

    saveFlights();
    res.json({ message: 'Flight updated', flight });
});
