# HACKUPC-2025

## 🗂️ Endpoints

### Base URL
```
http://<HOST_OR_IP>/
```

---

### 1. Get Flight Data

#### Endpoints:
- **GET** `/api/:flightNumber/data`
    Get data for a specific flight.
- **GET** `/api/ALL/data`
    Get all flights.

#### Response Examples:
- **Single Flight:**
    ```json
    {
        "data": {
            "flightNumber": "UA123",
            "origin": "...",
            "...": "..."
        }
    }
    ```

- **All Flights:**
    ```json
    [
        { "flightNumber": "...", ... },
        { "flightNumber": "...", ... }
    ]
    ```

---

### 2. Get Flight News

#### Endpoints:
- **GET** `/api/:flightNumber/news`
    Get news items for a specific flight.
- **GET** `/api/ALL/news`
    Get all news items.

#### Response Examples:
- **Specific Flight News:**
    ```json
    {
        "news": [
            { "id": "news001", "flightNumber": "UA123", ... },
            { "id": "news005", "flightNumber": "UA123", ... }
        ]
    }
    ```

- **All News:**
    ```json
    [
        { "id": "...", ... },
        { "id": "...", ... }
    ]
    ```

---

## 🔧 Admin Endpoints

> **Note:** No authentication is implemented—only call these from trusted/admin contexts.

### 3. Update Flight Data

#### Endpoint:
- **PATCH** `/api/admin/:flightNumber/data`

#### Request Body:
JSON object with one or more updatable fields:
```json
[
    "origin", "destination",
    "departureTime", "arrivalTime",
    "status", "gate", "door", "terminal"
]
```

#### Success Response:
```json
{ "message": "Flight updated", "flight": { ... } }
```

#### Example (JavaScript `fetch`):
```javascript
fetch(`/api/admin/UA123/data`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        departureTime: '2025-05-10T09:00:00Z',
        gate: 'G4'
    })
})
.then(r => r.json())
.then(console.log);
```

---

### 4. Create/Replace News for a Flight

#### Endpoint:
- **POST** `/api/admin/news`

#### Request Body:
JSON object with all required fields:
```json
{
    "id": "<unique-news-id>",
    "flightNumber": "<flightNumber>",
    "title": "<string>",
    "content": "<string>",
    "date": "<ISO timestamp>"
}
```

#### Success Response:
```json
{ "message": "News saved...", "article": { ... } }
```

#### Example (JavaScript `fetch`):
```javascript
fetch(`/api/admin/news`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        id: 'news123',
        flightNumber: 'UA123',
        title: 'Gate Changed',
        content: 'Flight UA123 now departs from Gate G4.',
        date: '2025-05-03T15:00:00Z'
    })
})
.then(r => r.json())
.then(console.log);
```

---

## ⚠️ Error Responses

- **400 Bad Request:** Missing/invalid parameters or attempting to change `flightNumber`/`id`.
    ```json
    { "error": "Flight number is required" }
    ```

- **404 Not Found:** Flight or news item doesn’t exist.
    ```json
    { "error": "Flight not found" }
    ```