<script lang="ts">
    import { onMount } from 'svelte';

    const GRID_SIZE = 20;
    let TILE_SIZE = $state(20); // pixels
    const INITIAL_SNAKE = [{ x: 10, y: 10 }];
    const INITIAL_FOOD = { x: 5, y: 5 };
    const INITIAL_DIRECTION = { x: 0, y: 0 }; // {x: 1, y: 0} for right, {x: -1, y: 0} for left, {x: 0, y: 1} for down, {x: 0, y: -1} for up
    const GAME_SPEED = 150; // milliseconds

    let snake = $state(INITIAL_SNAKE);
    let food = $state(INITIAL_FOOD);
    let direction = $state(INITIAL_DIRECTION);
    let score = $state(0);
    let gameOver = $state(true);
    let gameInterval: number;

    function startGame() {
        snake = INITIAL_SNAKE;
        food = INITIAL_FOOD;
        direction = INITIAL_DIRECTION;
        score = 0;
        gameOver = false;
        gameInterval = setInterval(gameLoop, GAME_SPEED);
    }

    function endGame() {
        clearInterval(gameInterval);
        gameOver = true;
    }

    function generateFood() {
        let newFood;
        do {
            newFood = {
                x: Math.floor(Math.random() * GRID_SIZE),
                y: Math.floor(Math.random() * GRID_SIZE)
            };
        } while (snake.some(segment => segment.x === newFood.x && segment.y === newFood.y));
        food = newFood;
    }

    function gameLoop() {
        const currentSnake = snake;
        const currentDirection = direction;
        const currentFood = food;
        let currentScore = score;

        const head = { x: currentSnake[0].x + currentDirection.x, y: currentSnake[0].y + currentDirection.y };

        // Check for wall collision
        if (head.x < 0 || head.x >= GRID_SIZE || head.y < 0 || head.y >= GRID_SIZE) {
            endGame();
            return;
        }

        // Check for self-collision
        if (currentSnake.some(segment => segment.x === head.x && segment.y === head.y)) {
            endGame();
            return;
        }

        const newSnake = [head, ...currentSnake];

        // Check for food collision
        if (head.x === currentFood.x && head.y === currentFood.y) {
            score = currentScore + 1;
            generateFood();
        } else {
            newSnake.pop(); // Remove tail if no food eaten
        }

        snake = newSnake;
    }

    function handleKeyDown(event: KeyboardEvent) {
        const currentDirection = direction;
        switch (event.key) {
            case 'ArrowUp':
                if (currentDirection.y === 0) direction = { x: 0, y: -1 };
                break;
            case 'ArrowDown':
                if (currentDirection.y === 0) direction = { x: 0, y: 1 };
                break;
            case 'ArrowLeft':
                if (currentDirection.x === 0) direction = { x: -1, y: 0 };
                break;
            case 'ArrowRight':
                if (currentDirection.x === 0) direction = { x: 1, y: 0 };
                break;
        }
    }

    onMount(() => {
        window.addEventListener('keydown', handleKeyDown);
        return () => {
            window.removeEventListener('keydown', handleKeyDown);
            clearInterval(gameInterval);
        };
    });
</script>

<div class="game-container">
    {#if gameOver}
        <div class="game-over">
            <h2>Game Over!</h2>
            <p>Score: {score}</p>
            <button onclick={startGame}>Play Again</button>
        </div>
    {:else}
        <div class="game-board" style="width: {GRID_SIZE * TILE_SIZE}px; height: {GRID_SIZE * TILE_SIZE}px;">
            {#each snake as segment}
                <div class="snake-segment" style="left: {segment.x * TILE_SIZE}px; top: {segment.y * TILE_SIZE}px;"></div>
            {/each}
            <div class="food" style="left: {food.x * TILE_SIZE}px; top: {food.y * TILE_SIZE}px;"></div>
        </div>
        <div class="score">Score: {score}</div>
    {/if}
</div>

<style>
    .game-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-top: 20px;
    }

    .game-board {
        position: relative;
        border: 5px solid #333;
        background-color: #1a1a1a;
        overflow: hidden;
        --tile-size: 20px;
    }

    .snake-segment {
        position: absolute;
        width: var(--tile-size);
        height: var(--tile-size);
        background-color: #4CAF50;
        border: 1px solid #333;
    }

    .food {
        position: absolute;
        width: var(--tile-size);
        height: var(--tile-size);
        background-color: #FFC107;
        border-radius: 50%;
    }

    .score {
        margin-top: 10px;
        font-size: 1.5em;
        color: #61dafb;
    }

    .game-over {
        text-align: center;
        padding: 20px;
        background-color: rgba(0, 0, 0, 0.7);
        border-radius: 10px;
    }

    .game-over h2 {
        color: #FF5722;
        margin-bottom: 10px;
    }

    .game-over p {
        font-size: 1.2em;
        margin-bottom: 20px;
    }

    .game-over button {
        padding: 10px 20px;
        font-size: 1.2em;
        background-color: #61dafb;
        color: #282c34;
        border: none;
        border-radius: 5px;
        cursor: pointer;
        transition: background-color 0.3s ease;
    }

    .game-over button:hover {
        background-color: #21a1f1;
    }
</style>
