import express from 'express';
import { getKanyeQuotes, getRandomQuote, getQuoteById } from '../services/quotesService.js';

const router = express.Router();

// Get all quotes
router.get('/', async (req, res) => {
  try {
    const quotes = await getKanyeQuotes();
    res.json({
      success: true,
      data: quotes,
      count: quotes.length
    });
  } catch (error) {
    console.error('Error fetching quotes:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to fetch quotes'
    });
  }
});

// Get random quote
router.get('/random', async (req, res) => {
  try {
    const quote = await getRandomQuote();
    res.json({
      success: true,
      data: quote
    });
  } catch (error) {
    console.error('Error fetching random quote:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to fetch random quote'
    });
  }
});

// Get quote by ID
router.get('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const quote = await getQuoteById(parseInt(id));
    
    if (!quote) {
      return res.status(404).json({
        success: false,
        error: 'Quote not found'
      });
    }
    
    res.json({
      success: true,
      data: quote
    });
  } catch (error) {
    console.error('Error fetching quote by ID:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to fetch quote'
    });
  }
});

export default router;
