---
name: Documentation Improvement
about: Suggest improvements to documentation
title: '[DOCS] '
labels: ['documentation']
assignees: ''
---

## 📚 Documentation Issue
What documentation needs improvement?

- [ ] API Documentation
- [ ] BlaBlaCar Bus API Integration
- [ ] Wiki Pages
- [ ] README
- [ ] Code Comments
- [ ] Examples
- [ ] Getting Started Guide
- [ ] Contributing Guidelines
- [ ] Transport API Integration
- [ ] Other: ___________

## 📍 Location
Where is the documentation that needs improvement?
- **File/Page**: [e.g. wiki/API-Documentation.md, README.md, src/main/scala/Document.scala]
- **Section**: [e.g. "Semigroup Operations", "Quick Start"]
- **Line numbers** (if applicable): [e.g. lines 45-60]

## ❌ Current Problem
What's wrong with the current documentation?

- [ ] Missing information
- [ ] Incorrect information  
- [ ] Unclear explanations
- [ ] Outdated examples
- [ ] Broken links
- [ ] Poor formatting
- [ ] Missing examples
- [ ] Other: ___________

## ✅ Proposed Improvement
What should be changed or added?

### Specific Changes
```markdown
<!-- Provide the exact text/examples you'd like to see -->
```

### Additional Context
Explain why this improvement would be helpful and who would benefit from it.

## 🎯 Target Audience
Who is the primary audience for this documentation?
- [ ] New users getting started
- [ ] Developers contributing to the project
- [ ] Advanced users looking for specific functionality
- [ ] API consumers
- [ ] Other: ___________

## 📝 Examples Needed
If examples would help, describe what kind:

```scala
// Example of what should be documented
val example = Document.someMethod(parameter)

// BlaBlaCar Bus API integration examples
val busRoute = BusRoute("Paris", "Lyon", BigDecimal("25.99"))
val routeDocument = processBusRoute(busRoute)

// API endpoints integration
val searchResults = busApiClient.searchRoutes(criteria)
val searchDocument = displaySearchResults(searchResults)
```

## 🚌 BlaBlaCar Bus API Context
This project integrates with the official BlaBlaCar Bus API (https://bus-api.blablacar.com) which provides:

### API Endpoints
- **Stops v1/v2/v3**: GET /v{1,2,3}/stops - Bus stop information with multilingual support
- **Fares v1/v2/v3**: GET /v{1,2,3}/fares - Fare information for route caching
- **Search v1/v2/v3**: POST /v{1,2,3}/search - Dynamic route search with passenger details
- **Booking**: GET /v3/fares/:id/book, /v3/search/:id/book - Redirect to booking

### Key Features
- **Authentication**: Bearer token via Authorization header
- **Multi-language**: Support for EN, FR, DE, IT, NL, ES
- **Geographic Data**: Latitude/longitude coordinates for stops
- **Meta Stations**: Parent stations with multiple child stops
- **Pricing**: Price in cents (EUR, GBP, CHF) with promo support
- **Real-time**: Live updates and availability
- **GTFS**: General Transit Feed Specification file support

### Data Structures
- **Stops**: ID, names (multilingual), coordinates, timezone, destinations
- **Fares**: Origin/destination, departure/arrival times, pricing, availability
- **Search**: Trip planning with passenger age-based pricing
- **Legs**: Multi-leg journeys with transfers
- **Booking**: Confirmation numbers, passenger details, payment info

### Cache Strategy
- **Stops**: Updated hourly, refresh twice per month
- **Prices (0-10 days)**: Updated every 2 hours
- **Prices (10-100 days)**: Updated every 24 hours

This integration enables the Document Matrix library to process and display transport data using functional programming patterns.

## 🔗 References
Are there any external resources that could help?
- Links to similar documentation
- References to relevant concepts
- Related issues or discussions

## 🚌 BlaBlaCar Bus API Integration Context

### Overview
The BlaBlaCar Bus API provides access to European coach services with the following capabilities:
- **Authentication**: Token-based API key authentication
- **Endpoint**: `https://bus-api.blablacar.com`
- **Current Version**: v3 (with v1 and v2 legacy support)
- **Compression**: Gzip compression recommended
- **Free to use**: Requires sign-up for API key

### Core API Methods

#### Stops Management
- **GET /v3/stops**: Get all bus stops with multilingual support
- **Features**: Meta station mechanism, geographic coordinates, time zones
- **Update Frequency**: Every hour (recommended: twice per month)

#### Search & Pricing
- **POST /v3/search**: Dynamic trip search with passenger pricing
- **GET /v3/fares**: Bulk fare data for caching
- **Features**: Promo pricing, refundable tickets, transfer support
- **Update Frequency**: Every 2 hours (next 10 days), daily (next 100 days)

#### Booking Integration
- **GET /v3/search/:trip_id/book**: Redirect to BlaBlaCar booking
- **Parameters**: affiliate_id, language, error_url
- **Languages**: en-GB, nl-NL, de-DE, es-ES, fr-FR, it-IT

### Data Structures

#### Stop Object
```json
{
  "id": 2,
  "short_name": "Paris CDG",
  "long_name": "Paris Aéroport Roissy-Charles-de-Gaulle",
  "time_zone": "Europe/Paris",
  "latitude": 49.009691,
  "longitude": 2.547925,
  "destinations_ids": [7, 12],
  "is_meta_gare": false,
  "address": "Address here"
}
```

#### Trip Object
```json
{
  "id": "unique-trip-id",
  "origin_id": 1,
  "destination_id": 17,
  "departure": "2015-08-15T16:45:00.000+02:00",
  "arrival": "2015-08-16T05:00:00.000+02:00",
  "available": true,
  "price_cents": 7250,
  "price_promo_cents": 6000,
  "is_promo": true,
  "is_refundable": false,
  "price_currency": "EUR",
  "legs": [...],
  "passengers": [...]
}
```

### Authentication Example
```bash
curl -H "Authorization: Token $API_KEY" \
    --compressed https://bus-api.blablacar.com/v3/stops
```

### Search Example
```bash
curl -H "Authorization: Token $API_KEY" \
    --header "Content-Type: application/json" \
    --compressed \
    --data '{
      "origin_id": 1,
      "destination_id": 17,
      "date": "2015-08-15",
      "passengers": [
        { "id": 1, "age": 30 },
        { "id": 2, "age": 30 },
        { "id": 3, "age": 1 }
      ]
    }' \
    https://bus-api.blablacar.com/v3/search
```

### Implementation Features in This Project
- **ZIO-based HTTP client** with proper error handling
- **JSON codecs** for all API data structures
- **Document processor** for converting API data to project documents
- **Mock data support** for development and testing
- **Comprehensive test suite** with property-based testing
- **HTTP server endpoints** for search, stops, and trip details
- **Interactive CLI** for API exploration and testing
- **Performance monitoring** and caching strategies

### Time Zones & Formatting
- **Times**: ISO 8601 format (e.g., `2015-04-16T19:00:00+02:00`)
- **Local Time Zones**: Departure/arrival times in stop's local timezone
- **UTC**: System timestamps (updated_at fields)
- **Dates**: YYYY-mm-dd format for API parameters

### Caching Strategy
- **Stops**: Update twice per month minimum
- **Next 10 days prices**: Update every 2 hours
- **Next 100 days prices**: Update daily
- **GTFS file**: Available without API key, updated several times daily

### Error Handling
- **Rate Limiting**: Respect API limits
- **Network Errors**: Implement retry logic with exponential backoff
- **Data Validation**: Validate all API responses
- **Fallback**: Use cached data when API unavailable

### Integration Checklist
- [ ] API key obtained and configured
- [ ] HTTP client with compression enabled
- [ ] JSON parsing for all response types
- [ ] Error handling and retry logic
- [ ] Caching mechanism implemented
- [ ] Time zone handling correct
- [ ] Booking redirect URLs configured
- [ ] Test suite covering all endpoints
- [ ] Performance monitoring in place
- [ ] Documentation updated

## ✅ Checklist
- [ ] I have identified the specific documentation that needs improvement
- [ ] I have explained what's wrong with the current version
- [ ] I have provided specific suggestions for improvement
- [ ] I have considered the target audience
