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

app.post('/api/data', (req, res) => {
  res.status(201).json({ received: req.body });
});

app.listen(80, () => {
  console.log('HTTP server running on port 80');
});
