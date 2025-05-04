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

// 1 Endpoint, return a phrase of a file where I have them stored. the round of the phrase has to be less than 10
// and +1 to all phrases
app.get('/api/game', (req, res) => {
    apiRequests++;
    bytesSent += res.get('Content-Length') || 0;

    const phrases = JSON.parse(fs.readFileSync('phrases.json', 'utf-8'));
    const randomPhrase = phrases[Math.floor(Math.random() * phrases.length)];
    const updatedPhrases = phrases.map(phrase => ({ ...phrase, round: phrase.round + 1 }));

    fs.writeFileSync('phrases.json', JSON.stringify(updatedPhrases, null, 2), 'utf8');
    res.json(randomPhrase);
});

// 2 Endpoint, return all phrases that have a round of 10 or more
app.get('/api/game/all', (req, res) => {
    apiRequests++;
    bytesSent += res.get('Content-Length') || 0;

    const phrases = JSON.parse(fs.readFileSync('phrases.json', 'utf-8'));
    const filteredPhrases = phrases.filter(phrase => phrase.round >= 10);

    if (filteredPhrases.length === 0) return res.status(404).json({ error: 'No phrases found with round >= 10' });

    res.json(filteredPhrases);
});

// 3 Endpoint, to create new phrases
app.post('/api/game', (req, res) => {
    apiRequests++;
    bytesSent += res.get('Content-Length') || 0;

    const { phrase, round } = req.body;

    if (!phrase || !round) return res.status(400).json({ error: 'Missing required fields' });

    const newPhrase = { phrase, round };
    const phrases = JSON.parse(fs.readFileSync('phrases.json', 'utf-8'));
    phrases.push(newPhrase);

    fs.writeFileSync('phrases.json', JSON.stringify(phrases, null, 2), 'utf8');
    res.status(201).json({ message: 'Phrase created', phrase: newPhrase });
});

// ===== Server =====
app.listen(80, () => {
    console.log('HTTP server running on port 80');
});
