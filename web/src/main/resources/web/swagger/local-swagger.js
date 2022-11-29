window.onload = function() {
  // Begin Swagger UI call region
  const ui = SwaggerUIBundle({
    urls: [
      {"url": "/fpabakus/api/openapi.yaml", "name": "ABAKUS intern API"},
      {"url": "/fpabakus/ekstern/api/openapi.yaml", "name": "ABAKUS ekstern API"}
    ],
    explorer: true,
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout"
  });
  // End Swagger UI call region

  window.ui = ui
}
