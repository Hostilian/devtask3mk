// Svelte 5 runes-based store for quotes management
export interface Quote {
  id: number
  text: string
  category: string
  year: number
  source?: string
}

export interface QuotesState {
  currentQuote: Quote | null
  allQuotes: Quote[]
  isLoading: boolean
  error: string | null
}

// Create reactive state using Svelte 5 runes
class QuotesStore {
  private state = $state<QuotesState>({
    currentQuote: null,
    allQuotes: [],
    isLoading: false,
    error: null
  })
  
  // Getters using derived runes
  get currentQuote() {
    return this.state.currentQuote
  }
  
  get allQuotes() {
    return this.state.allQuotes
  }
  
  get isLoading() {
    return this.state.isLoading
  }
  
  get error() {
    return this.state.error
  }
  
  // Actions
  setLoading(loading: boolean) {
    this.state.isLoading = loading
  }
  
  setError(error: string | null) {
    this.state.error = error
  }
  
  setCurrentQuote(quote: Quote | null) {
    this.state.currentQuote = quote
  }
  
  setAllQuotes(quotes: Quote[]) {
    this.state.allQuotes = quotes
  }
  
  // API calls
  async fetchRandomQuote() {
    this.setLoading(true)
    this.setError(null)
    
    try {
      const response = await fetch('/api/quotes/random')
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const data = await response.json()
      
      if (data.success) {
        this.setCurrentQuote(data.data)
      } else {
        throw new Error(data.error || 'Failed to fetch quote')
      }
    } catch (error) {
      console.error('Error fetching random quote:', error)
      this.setError(error instanceof Error ? error.message : 'Failed to fetch quote')
    } finally {
      this.setLoading(false)
    }
  }
  
  async fetchAllQuotes() {
    this.setLoading(true)
    this.setError(null)
    
    try {
      const response = await fetch('/api/quotes')
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const data = await response.json()
      
      if (data.success) {
        this.setAllQuotes(data.data)
      } else {
        throw new Error(data.error || 'Failed to fetch quotes')
      }
    } catch (error) {
      console.error('Error fetching all quotes:', error)
      this.setError(error instanceof Error ? error.message : 'Failed to fetch quotes')
    } finally {
      this.setLoading(false)
    }
  }
  
  async fetchQuoteById(id: number) {
    this.setLoading(true)
    this.setError(null)
    
    try {
      const response = await fetch(`/api/quotes/${id}`)
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const data = await response.json()
      
      if (data.success) {
        this.setCurrentQuote(data.data)
      } else {
        throw new Error(data.error || 'Failed to fetch quote')
      }
    } catch (error) {
      console.error('Error fetching quote by ID:', error)
      this.setError(error instanceof Error ? error.message : 'Failed to fetch quote')
    } finally {
      this.setLoading(false)
    }
  }
}

// Export singleton instance
export const quotesStore = new QuotesStore()
