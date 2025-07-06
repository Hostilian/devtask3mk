import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import dotenv from 'dotenv';
import quotesRouter from './routes/quotes.js';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3001;

// Middleware
app.use(helmet());
app.use(cors({
  origin: process.env.FRONTEND_URL || 'http://localhost:5173',
  credentials: true
}));
app.use(express.json());

// Routes
app.use('/api/quotes', quotesRouter);

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'OK', message: 'Kanye Quotes API is running' });
});

// Root endpoint
app.get('/', (req, res) => {
  res.json({ 
    message: 'Welcome to the Kanye Quotes API', 
    endpoints: [
      'GET /health - Health check',
      'GET /api/quotes - Get all quotes',
      'GET /api/quotes/random - Get random quote',
      'GET /api/quotes/:id - Get quote by ID'
    ]
  });
});

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({ error: 'Endpoint not found' });
});

// Error handler
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ error: 'Something went wrong!' });
});

app.listen(PORT, () => {
  console.log(`🎤 Kanye Quotes API server running on port ${PORT}`);
  console.log(`📖 Visit http://localhost:${PORT} for API documentation`);
});
