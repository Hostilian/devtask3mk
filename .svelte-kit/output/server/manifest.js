export const manifest = (() => {
function __memo(fn) {
	let value;
	return () => value ??= (value = fn());
}

return {
	appDir: "_app",
	appPath: "_app",
	assets: new Set([]),
	mimeTypes: {},
	_: {
		client: {start:"_app/immutable/entry/start.B-a0WkNC.js",app:"_app/immutable/entry/app.B589Owy5.js",imports:["_app/immutable/entry/start.B-a0WkNC.js","_app/immutable/chunks/DWX0BDaT.js","_app/immutable/chunks/Cm3C-Kfe.js","_app/immutable/chunks/B9SQDHgi.js","_app/immutable/entry/app.B589Owy5.js","_app/immutable/chunks/B9SQDHgi.js","_app/immutable/chunks/Cm3C-Kfe.js","_app/immutable/chunks/CWj6FrbW.js","_app/immutable/chunks/CeBR92tR.js"],stylesheets:[],fonts:[],uses_env_dynamic_public:false},
		nodes: [
			__memo(() => import('./nodes/0.js')),
			__memo(() => import('./nodes/1.js')),
			__memo(() => import('./nodes/2.js'))
		],
		routes: [
			{
				id: "/",
				pattern: /^\/$/,
				params: [],
				page: { layouts: [0,], errors: [1,], leaf: 2 },
				endpoint: null
			}
		],
		prerendered_routes: new Set([]),
		matchers: async () => {
			
			return {  };
		},
		server_assets: {}
	}
}
})();
