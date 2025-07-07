<script lang="ts">
    import Game from '$lib/Game.svelte';
</script>

<main>
    <h1>Snake Game</h1>
    <Game />
</main>

<style>
    main {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        min-height: 100vh;
        background-color: #282c34;
        color: white;
        font-family: 'Arial', sans-serif;
    }

    h1 {
        color: #61dafb;
        margin-bottom: 20px;
    }
</style>
