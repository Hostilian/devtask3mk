

export const index = 0;
let component_cache;
export const component = async () => component_cache ??= (await import('../entries/fallbacks/layout.svelte.js')).default;
export const imports = ["_app/immutable/nodes/0.Bpq8Q-Qu.js","_app/immutable/chunks/CWj6FrbW.js","_app/immutable/chunks/B9SQDHgi.js"];
export const stylesheets = [];
export const fonts = [];
