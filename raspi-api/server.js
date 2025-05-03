const express = require('express');
const fs = require('fs');
const crypto = require('crypto'); // For signing data

const app = express();
app.use(express.json());

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
    const { flightNumber } = req.params;
    if (!flightNumber) return res.status(400).json({ error: 'Flight number is required' });

    if (flightNumber === 'ALL') {
        const signature = signData(data);
        return res.json({ data: data, signature });
    }

    const flight = data.find(f => f.flightNumber === flightNumber);
    if (!flight) return res.status(404).json({ error: 'Flight not found' });

    const signature = signData(flight);
    res.json({ data: flight, signature });
});

// GET /api/:flightNumber/news
app.get('/api/:flightNumber/news', (req, res) => {
    const { flightNumber } = req.params;
    if (!flightNumber) return res.status(400).json({ error: 'Flight number is required' });

    if (flightNumber === 'ALL') {
        const signature = signData(news);
        return res.json({ news: news, signature });
    }

    const flightNews = news.filter(n => n.flightNumber === flightNumber);
    if (flightNews.length === 0) return res.status(404).json({ error: 'Flight news not found' });

    const signature = signData(flightNews);
    res.json({ news: flightNews, signature });
});

// PATCH /api/admin/:flightNumber/data
app.patch('/api/admin/:flightNumber/data', (req, res) => {
    const { flightNumber } = req.params;
    const updates = req.body;

    const flight = data.find(f => f.flightNumber === flightNumber);
    if (!flight) return res.status(404).json({ error: 'Flight not found' });

    if ('flightNumber' in updates) {
        return res.status(400).json({ error: 'Flight number cannot be modified' });
    }

    const allowedFields = ['origin', 'destination', 'departureTime', 'arrivalTime', 'status', 'gate', 'door', 'terminal'];
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

// ===== Server =====
app.listen(80, () => {
    console.log('HTTP server running on port 80');
});
