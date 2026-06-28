# EvoCharge API Contract v1

Base URL:

- **Local:** `http://localhost:8080`
- **Production:** `https://d8061ggv2y910.cloudfront.net/api/v1` (CloudFront proxies `/api/*` to ALB → ECS)

## Endpoints

### Health
- `GET /api/v1/health`
- Response: `{ "status": "UP", "service": "evocharge-api", "version": "1.0.0" }`

### Operators
- `GET /api/v1/operators`
- Response: `Operator[]`

### Stations
- `GET /api/v1/stations?city=&operator=&status=&connector=&search=`
- `GET /api/v1/stations/cities`
- `GET /api/v1/stations/{id}`
- `GET /api/v1/stations/nearby?lat=&lng=&radius=10&city=`

### Recommendations
- `POST /api/v1/recommend`
- Body: `{ "lat": 6.4281, "lng": 3.4219, "batteryPercent": 35, "connectorType": "CCS2" }`
- Response: `{ "recommendations": RankedStation[] }`

### Analytics
- `GET /api/v1/analytics/summary`
- `GET /api/v1/analytics/demand-by-area?city=`

### Live Events (SSE)
- `GET /api/v1/events/stream`
- Events: `connected`, `status` (StatusEvent payload)

### AI Advisor
- `POST /api/v1/advisor`
- Body: `{ "query": "...", "lat": 6.4281, "lng": 3.4219 }`
- Response: `{ "answer": "...", "stations": RankedStation[] }`

## Station Schema

```json
{
  "id": "st-001",
  "name": "EVNetwork VI Hub",
  "operatorId": "op-evnetwork",
  "operatorName": "EVNetwork NG",
  "lat": 6.4281,
  "lng": 3.4219,
  "address": "Adeola Odeku St, Victoria Island",
  "city": "Lagos",
  "area": "Victoria Island",
  "status": "AVAILABLE",
  "connectors": ["CCS2", "Type2"],
  "powerKw": 150,
  "waitMinutes": 5,
  "reliabilityScore": 92,
  "gridStatus": "STABLE",
  "evoScore": 87.5,
  "lastUpdated": "2026-06-06T08:00:00Z"
}
```

## EvoScore Weights
- Distance: 30%
- Availability: 25%
- Wait time: 20%
- Reliability: 15%
- Connector match: 10%
