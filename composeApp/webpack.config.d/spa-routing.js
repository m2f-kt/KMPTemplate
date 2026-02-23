// Enable SPA routing for the dev server
// This redirects all 404s to index.html so client-side routing works,
// while allowing static assets to be served from their correct paths
config.devServer = config.devServer || {};
config.devServer.historyApiFallback = {
    // Rewrite resource requests that have a path prefix (like /invite/composeResources)
    // to serve from root (like /composeResources)
    rewrites: [
        // Static assets should be served from root
        { from: /^\/[^/]+\/composeResources\/(.*)$/, to: '/composeResources/$1' },
        { from: /^\/[^/]+\/[^/]+\/composeResources\/(.*)$/, to: '/composeResources/$1' },
        // Everything else falls back to index.html for client-side routing
        { from: /./, to: '/index.html' }
    ]
};
