<script lang="ts">
  import Header from './lib/components/Header.svelte'
  import QuoteDisplay from './lib/components/QuoteDisplay.svelte'
  import QuoteControls from './lib/components/QuoteControls.svelte'
  import QuotesList from './lib/components/QuotesList.svelte'
  import LoadingSpinner from './lib/components/LoadingSpinner.svelte'
  import ErrorMessage from './lib/components/ErrorMessage.svelte'
  
  import { quotesStore } from './lib/stores/quotesStore.js'
  
  // Using Svelte 5 runes for reactive state
  let currentView = $state('random') // 'random' | 'all'
  let isLoading = $state(false)
  let error = $state<string | null>(null)
  
  // Reactive computed values using runes
  let currentQuote = $derived(quotesStore.currentQuote)
  let allQuotes = $derived(quotesStore.allQuotes)
  
  // Handle view changes
  function handleViewChange(view: string) {
    currentView = view
    error = null
  }
  
  // Handle loading state changes
  function handleLoadingChange(loading: boolean) {
    isLoading = loading
  }
  
  // Handle error state changes
  function handleError(errorMessage: string | null) {
    error = errorMessage
  }
</script>

<main class="app">
  <Header />
  
  <div class="container">
    <QuoteControls 
      {currentView}
      onViewChange={handleViewChange}
    />
    
    {#if error}
      <ErrorMessage 
        message={error}
        onDismiss={() => handleError(null)}
      />
    {/if}
    
    {#if isLoading}
      <LoadingSpinner />
    {:else if currentView === 'random'}
      <QuoteDisplay 
        quote={currentQuote}
        onLoadingChange={handleLoadingChange}
        onError={handleError}
      />
    {:else if currentView === 'all'}
      <QuotesList 
        quotes={allQuotes}
        onLoadingChange={handleLoadingChange}
        onError={handleError}
      />
    {/if}
  </div>
</main>

<style>
  :global(*) {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }
  
  :global(body) {
    font-family: 'Inter', sans-serif;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    min-height: 100vh;
    color: #333;
  }
  
  .app {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
  }
  
  .container {
    flex: 1;
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
    width: 100%;
  }
  
  @media (max-width: 768px) {
    .container {
      padding: 1rem;
    }
  }
</style>
