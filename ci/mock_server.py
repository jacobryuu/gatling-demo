#!/usr/bin/env python3
"""
Tiny stub HTTP server used by CI so the Gatling smoke run actually exercises
request paths instead of being skipped.

Endpoints:
  POST /login          -> 200 {"token":"fake-token"}
  GET  /items?q=...    -> 200 {"items":[{"id":"1"},{"id":"2"}]}
  GET  /items/<id>     -> 200 {"id":"<id>"}
  POST /transactions   -> 201 {"id":"tx-<uuid>"}
"""
from http.server import BaseHTTPRequestHandler, HTTPServer
import json, sys, uuid

class Handler(BaseHTTPRequestHandler):
    def _json(self, status, body):
        payload = json.dumps(body).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(payload)))
        self.end_headers()
        self.wfile.write(payload)

    def do_GET(self):
        path = self.path.split("?", 1)[0]
        if path == "/items":
            return self._json(200, {"items": [{"id": "1"}, {"id": "2"}]})
        if path.startswith("/items/"):
            return self._json(200, {"id": path.rsplit("/", 1)[1]})
        return self._json(404, {"error": "not found"})

    def do_POST(self):
        length = int(self.headers.get("Content-Length", "0"))
        _ = self.rfile.read(length)
        if self.path == "/login":
            return self._json(200, {"token": "fake-token"})
        if self.path == "/transactions":
            return self._json(201, {"id": f"tx-{uuid.uuid4()}"})
        return self._json(404, {"error": "not found"})

    def log_message(self, *_args, **_kwargs):
        pass  # silence stderr

if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8080
    print(f"[mock-server] listening on :{port}", flush=True)
    HTTPServer(("0.0.0.0", port), Handler).serve_forever()
