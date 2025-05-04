const express = require('express');
const fs = require('fs');
const crypto = require('crypto'); // For signing data

const app = express();
app.use(express.json());

// Stats
let apiRequests = 0;
let bytesSent = 0;

// Load data
let data = JSON.parse(fs.readFileSync('data.json', 'utf-8'));
let news = JSON.parse(fs.readFileSync('news.json', 'utf-8'));

// Load private key (ensure the private key is securely stored)
const privateKey = fs.readFileSync('private_key.pem', 'utf8');

// Utility Functions
function saveFlights() {
    fs.writeFileSync('data.json', JSON.stringify(data, null, 2), 'utf8');
}

function saveNews() {
    fs.writeFileSync('news.json', JSON.stringify(news, null, 2), 'utf8');
}

// Sign the response data
function signData(data) {
    const sign = crypto.createSign('SHA256');
    sign.update(JSON.stringify(data)); // Sign the stringified data
    sign.end();
    const signature = sign.sign(privateKey, 'base64');
    return signature;
}

// ===== Routes =====

// GET /api/:flightNumber/data
app.get('/api/:flightNumber/data', (req, res) => {
    apiRequests++;
    bytesSent += res.get('Content-Length') || 0;
    const { flightNumber } = req.params;
    if (!flightNumber) return res.status(400).json({ error: 'Flight number is required' });

    if (flightNumber === 'ALL') {
        const signature = signData(data);
        const response = [...data, { signature }];
        return res.json(response);
    }

    const flight = data.find(f => f.flightNumber === flightNumber);
    if (!flight) return res.status(404).json({ error: 'Flight not found' });

    const signature = signData(flight);
    const response = { ...flight, signature };
    res.json(response);
});

// GET /api/:flightNumber/news
app.get('/api/:flightNumber/news', (req, res) => {
    apiRequests++;
    bytesSent += res.get('Content-Length') || 0;
    const { flightNumber } = req.params;
    if (!flightNumber) return res.status(400).json({ error: 'Flight number is required' });

    if (flightNumber === 'ALL') {
        const signature = signData(news);
        const response = [...news, { signature }];
        return res.json(response);
    }

    const flightNews = news.filter(n => n.flightNumber === flightNumber);
    if (flightNews.length === 0) return res.status(404).json({ error: 'Flight news not found' });

    const signature = signData(flightNews);
    const response = [...flightNews, { signature }];
    res.json(response);
});

// PATCH /api/admin/:flightNumber/data
app.patch('/api/admin/:flightNumber/data', (req, res) => {
    apiRequests++;
    bytesSent += res.get('Content-Length') || 0;

    const { flightNumber } = req.params;
    const updates = req.body;

    const flight = data.find(f => f.flightNumber === flightNumber);
    if (!flight) return res.status(404).json({ error: 'Flight not found' });

    if ('flightNumber' in updates) {
        return res.status(400).json({ error: 'Flight number cannot be modified' });
    }

    const allowedFields = ['flightNumber', 'originFull', 'originShort', 'departureTime', 'destinationFull', 'destinationShort', 'landingTime', 'status', 'date'];
    for (const key of Object.keys(updates)) {
        if (allowedFields.includes(key)) {
            flight[key] = updates[key];
        } else {
            return res.status(400).json({ error: `Invalid field: ${key}` });
        }
    }

    saveFlights();
    const signature = signData(flight);
    res.json({ message: 'Flight updated', flight, signature });
});

// end point to create flights
app.post('/api/admin/data', (req, res) => {
    apiRequests++;
    bytesSent += res.get('Content-Length') || 0;

    const { flightNumber, originFull, originShort, departureTime, destinationFull, destinationShort, landingTime, status, date } = req.body;

    if (!flightNumber || !originFull || !originShort || !departureTime || !destinationFull || !destinationShort || !landingTime || !status || !date) {
        return res.status(400).json({ error: 'Missing required fields' });
    }

    const newFlight = { flightNumber, originFull, originShort, departureTime, destinationFull, destinationShort, landingTime, status, date };
    data.push(newFlight);

    saveFlights();
    const signature = signData(newFlight);

    res.status(201).json({ message: 'Flight created', flight: newFlight, signature });
});

app.post('/api/admin/news', (req, res) => {
    apiRequests++;
    bytesSent += res.get('Content-Length') || 0;

    const { id, flightNumber, title, content, date } = req.body;

    if (!id || !flightNumber || !title || !content || !date) {
        return res.status(400).json({ error: 'Missing required fields' });
    }

    // Replace existing news for that flight
    news = news.filter(n => n.flightNumber !== flightNumber);

    const newArticle = { id, flightNumber, title, content, date };
    news.push(newArticle);

    saveNews();
    const signature = signData(newArticle);

    res.status(201).json({ message: 'News saved (old one replaced if existed)', article: newArticle, signature });
});

// ===== Game =====

// endpoint that gets you a random game, either exists or it is newly created
app.get('/api/game', (req, res) => {
    apiRequests++;
    bytesSent += res.get('Content-Length') || 0;

    const phrases = JSON.parse(fs.readFileSync('phrases.json', 'utf-8'));
    const randomPhrase = phrases[Math.floor(Math.random() * phrases.length)];

    if (!randomPhrase) return res.status(404).json({ error: 'No phrases found' });

    res.json(randomPhrase);
});

// endpoint to add a new phrase to the game and increment the round, the phrases get concatenated if the round is les than 10
app.post('/api/game/add', (req, res) => {
    apiRequests++;
    bytesSent += res.get('Content-Length') || 0;

    const { phrase, round } = req.body;

    if (!phrase || !round) return res.status(400).json({ error: 'Missing required fields' });

    const phrases = JSON.parse(fs.readFileSync('phrases.json', 'utf-8'));
    const lastPhrase = phrases[phrases.length - 1];

    if (lastPhrase.round >= 10) return res.status(400).json({ error: 'Maximum rounds reached' });

    const newPhrase = { phrase, round: lastPhrase.round + 1 };
    phrases.push(newPhrase);

    fs.writeFileSync('phrases.json', JSON.stringify(phrases, null, 2), 'utf8');
    res.status(201).json({ message: 'Phrase added', phrase: newPhrase });
});

// endpoint to get the games that have ended
app.get('/api/game/ended', (req, res) => {
    apiRequests++;
    bytesSent += res.get('Content-Length') || 0;

    const phrases = JSON.parse(fs.readFileSync('phrases.json', 'utf-8'));
    const endedPhrases = phrases.filter(p => p.round >= 10);

    if (endedPhrases.length === 0) return res.status(404).json({ error: 'No ended games found' });

    res.json(endedPhrases);
});

// ===== Server =====
app.listen(80, () => {
    console.log('HTTP server running on port 80');
});
