// Enable SPA routing for the dev server
// This redirects all 404s to index.html so client-side routing works
config.devServer = config.devServer || {};
config.devServer.historyApiFallback = true;
