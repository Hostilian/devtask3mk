<script lang="ts">
    import { onMount, onDestroy } from 'svelte';
    import { readable, writable } from 'svelte/store';

    const TILE_SIZE = 20;
    const BOARD_SIZE = 20;
    const INITIAL_SNAKE = [{ x: 10, y: 10 }];
    const INITIAL_DIRECTION = { x: 0, y: -1 }; // Up
    const GAME_SPEED = 150; // Milliseconds

    let canvas: HTMLCanvasElement;
    let ctx: CanvasRenderingContext2D;
    let gameInterval: number;

    // Game state using Svelte 5 runes
    let snake = $state(INITIAL_SNAKE);
    let food = $state({ x: 5, y: 5 });
    let direction = $state(INITIAL_DIRECTION);
    let score = $state(0);
    let gameOver = $state(false);
    let gameStarted = $state(false);

    function generateFood() {
        let newFood;
        do {
            newFood = {
                x: Math.floor(Math.random() * BOARD_SIZE),
                y: Math.floor(Math.random() * BOARD_SIZE)
            };
        } while (snake.some(segment => segment.x === newFood.x && segment.y === newFood.y));
        food = newFood;
    }

    function updateGame() {
        if (gameOver) return;

        const head = { x: snake[0].x + direction.x, y: snake[0].y + direction.y };

        // Check for collisions with walls
        if (head.x < 0 || head.x >= BOARD_SIZE || head.y < 0 || head.y >= BOARD_SIZE) {
            gameOver = true;
            return;
        }

        // Check for collisions with self
        if (snake.some(segment => segment.x === head.x && segment.y === head.y)) {
            gameOver = true;
            return;
        }

        snake = [head, ...snake];

        // Check for food collision
        if (head.x === food.x && head.y === food.y) {
            score++;
            generateFood();
        } else {
            snake = snake.slice(0, -1);
        }

        drawGame();
    }

    function drawGame() {
        if (!ctx) return;

        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Draw snake
        ctx.fillStyle = '#61afef';
        snake.forEach(segment => {
            ctx.fillRect(segment.x * TILE_SIZE, segment.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        });

        // Draw food
        ctx.fillStyle = '#e06c75';
        ctx.fillRect(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    function handleKeyDown(event: KeyboardEvent) {
        if (gameOver) return;

        const newDirection = {
            ArrowUp: { x: 0, y: -1 },
            ArrowDown: { x: 0, y: 1 },
            ArrowLeft: { x: -1, y: 0 },
            ArrowRight: { x: 1, y: 0 }
        }[event.key];

        if (newDirection) {
            // Prevent reversing direction
            if (newDirection.x !== -direction.x || newDirection.y !== -direction.y) {
                direction = newDirection;
            }
        }
    }

    function startGame() {
        snake = INITIAL_SNAKE;
        direction = INITIAL_DIRECTION;
        score = 0;
        gameOver = false;
        gameStarted = true;
        generateFood();
        drawGame();
        clearInterval(gameInterval);
        gameInterval = setInterval(updateGame, GAME_SPEED);
    }

    onMount(() => {
        ctx = canvas.getContext('2d')!;
        canvas.width = BOARD_SIZE * TILE_SIZE;
        canvas.height = BOARD_SIZE * TILE_SIZE;
        window.addEventListener('keydown', handleKeyDown);
        drawGame(); // Initial draw
    });

    onDestroy(() => {
        clearInterval(gameInterval);
        window.removeEventListener('keydown', handleKeyDown);
    });
</script>

<div class="game-container">
    <h1>Snake Game</h1>
    <canvas bind:this={canvas}></canvas>
    <div class="score">Score: {score}</div>

    {#if !gameStarted}
        <button on:click={startGame}>Start Game</button>
    {:else if gameOver}
        <div class="game-message">Game Over!</div>
        <button on:click={startGame}>Play Again</button>
    {/if}
</div>

<style>
    /* Styles are in app.css */
</style>
