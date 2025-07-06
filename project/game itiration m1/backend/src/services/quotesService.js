import fetch from 'node-fetch';

// Curated Kanye West quotes
const kanyeQuotes = [
  {
    id: 1,
    text: "I'm not a businessman, I'm a business, man!",
    category: "business",
    year: 2013
  },
  {
    id: 2,
    text: "My greatest pain in life is that I will never be able to see myself perform live.",
    category: "philosophy",
    year: 2015
  },
  {
    id: 3,
    text: "I feel like I'm too busy writing history to read it.",
    category: "creativity",
    year: 2016
  },
  {
    id: 4,
    text: "Sometimes people write novels and they just be so wordy and so self-absorbed.",
    category: "literature",
    year: 2014
  },
  {
    id: 5,
    text: "I am Warhol. I am the number one most impactful artist of our generation.",
    category: "art",
    year: 2013
  },
  {
    id: 6,
    text: "The world is our family... We're the royal family of the world.",
    category: "philosophy",
    year: 2018
  },
  {
    id: 7,
    text: "I refuse to accept other people's ideas of happiness for me.",
    category: "philosophy",
    year: 2012
  },
  {
    id: 8,
    text: "Creative output, you know, is just being able to get most of yourself out.",
    category: "creativity",
    year: 2019
  },
  {
    id: 9,
    text: "I still think I am the greatest.",
    category: "confidence",
    year: 2016
  },
  {
    id: 10,
    text: "Everything you do in life stems from either fear or love.",
    category: "philosophy",
    year: 2014
  },
  {
    id: 11,
    text: "I am God's vessel. But my greatest pain in life is that I will never be able to see myself perform live.",
    category: "philosophy",
    year: 2013
  },
  {
    id: 12,
    text: "The system is broken. I am not a part of it.",
    category: "society",
    year: 2020
  },
  {
    id: 13,
    text: "I make awesome music and I'm an awesome dad.",
    category: "personal",
    year: 2018
  },
  {
    id: 14,
    text: "Keep your nose out the sky, keep your heart to god, and keep your face to the rising sun.",
    category: "wisdom",
    year: 2011
  },
  {
    id: 15,
    text: "I'm doing pretty good as far as geniuses go... I'm like a machine.",
    category: "confidence",
    year: 2013
  }
];

// External API for additional quotes (Kanye.rest)
const KANYE_API_URL = 'https://api.kanye.rest';

export const getKanyeQuotes = async () => {
  try {
    // Return our curated quotes for now
    // In production, you might want to combine with external API
    return kanyeQuotes;
  } catch (error) {
    console.error('Error fetching quotes:', error);
    throw new Error('Failed to fetch quotes');
  }
};

export const getRandomQuote = async () => {
  try {
    // First try external API
    try {
      const response = await fetch(KANYE_API_URL);
      if (response.ok) {
        const data = await response.json();
        return {
          id: Date.now(), // Generate temporary ID
          text: data.quote,
          category: "external",
          year: new Date().getFullYear(),
          source: "kanye.rest"
        };
      }
    } catch (apiError) {
      console.log('External API unavailable, using local quotes');
    }
    
    // Fallback to local quotes
    const randomIndex = Math.floor(Math.random() * kanyeQuotes.length);
    return kanyeQuotes[randomIndex];
  } catch (error) {
    console.error('Error fetching random quote:', error);
    throw new Error('Failed to fetch random quote');
  }
};

export const getQuoteById = async (id) => {
  try {
    return kanyeQuotes.find(quote => quote.id === id) || null;
  } catch (error) {
    console.error('Error fetching quote by ID:', error);
    throw new Error('Failed to fetch quote');
  }
};

export const getQuotesByCategory = async (category) => {
  try {
    return kanyeQuotes.filter(quote => quote.category === category);
  } catch (error) {
    console.error('Error fetching quotes by category:', error);
    throw new Error('Failed to fetch quotes by category');
  }
};
