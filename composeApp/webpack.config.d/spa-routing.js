// SPA routing configuration for webpack dev server
// Handles deep links like /invite/accept?token=... by serving index.html for non-file requests
// The <base href="/"> in index.html ensures all relative URLs resolve from root

config.devServer = config.devServer || {};
config.devServer.historyApiFallback = true;
