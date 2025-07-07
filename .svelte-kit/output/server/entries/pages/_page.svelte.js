import "clsx";
import { e as escape_html } from "../../chunks/escaping.js";
import { v as pop, t as push } from "../../chunks/index.js";
function Game($$payload, $$props) {
  push();
  let score = 0;
  $$payload.out += `<div class="game-container svelte-np5jdi">`;
  {
    $$payload.out += "<!--[-->";
    $$payload.out += `<div class="game-over svelte-np5jdi"><h2 class="svelte-np5jdi">Game Over!</h2> <p class="svelte-np5jdi">Score: ${escape_html(score)}</p> <button class="svelte-np5jdi">Play Again</button></div>`;
  }
  $$payload.out += `<!--]--></div>`;
  pop();
}
function _page($$payload) {
  $$payload.out += `<main class="svelte-z7wa5z"><h1 class="svelte-z7wa5z">Snake Game</h1> `;
  Game($$payload);
  $$payload.out += `<!----></main>`;
}
export {
  _page as default
};
