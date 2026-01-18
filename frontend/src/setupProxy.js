const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  app.use(
    '/ws',
    createProxyMiddleware({
      target: 'http://localhost:8080',
      ws: true, // Habilita WebSocket proxying
      changeOrigin: true,
      // Opcional: reduce el timeout para conexiones más rápidas
      proxyTimeout: 5000,
      timeout: 5000,
    })
  );
};