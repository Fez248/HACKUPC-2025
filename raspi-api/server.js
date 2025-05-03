const express = require('express');
const fs = require('fs');

const app = express();
app.use(express.json());

let data = JSON.parse(fs.readFileSync('data.json', 'utf-8'));
let news = JSON.parse(fs.readFileSync('news.json', 'utf-8'));

// ===== Utility Functions =====
function saveFlights() {
    fs.writeFileSync('data.json', JSON.stringify(data, null, 2), 'utf8');
}

function saveNews() {
    fs.writeFileSync('news.json', JSON.stringify(news, null, 2), 'utf8');
}

// ===== Routes =====

// GET /api/:flightNumber/data
app.get('/api/:flightNumber/data', (req, res) => {
    const { flightNumber } = req.params;
    if (!flightNumber) return res.status(400).json({ error: 'Flight number is required' });

    if (flightNumber === 'ALL') return res.json(data);

    const flight = data.find(f => f.flightNumber === flightNumber);
    if (!flight) return res.status(404).json({ error: 'Flight not found' });

    res.json({ data: flight });
});

// GET /api/:flightNumber/news
app.get('/api/:flightNumber/news', (req, res) => {
    const { flightNumber } = req.params;
    if (!flightNumber) return res.status(400).json({ error: 'Flight number is required' });

    if (flightNumber === 'ALL') return res.json(news);

    const flightNews = news.filter(n => n.flightNumber === flightNumber);
    if (flightNews.length === 0) return res.status(404).json({ error: 'Flight news not found' });

    res.json({ news: flightNews });
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
    res.json({ message: 'Flight updated', flight });
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

    saveNews();  // This must be defined before this route

    res.status(201).json({ message: 'News saved (old one replaced if existed)', article: newArticle });
});

// ===== Server =====
app.listen(80, () => {
    console.log('HTTP server running on port 80');
});
