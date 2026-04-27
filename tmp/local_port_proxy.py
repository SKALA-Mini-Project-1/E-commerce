from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import http.client


ROUTES = [
    ("/api/users", ("127.0.0.1", 18081)),
    ("/api/products", ("127.0.0.1", 18082)),
    ("/api/orders", ("127.0.0.1", 18083)),
    ("/api/payments", ("127.0.0.1", 18084)),
    ("/frontend", ("127.0.0.1", 18080)),
]


def resolve_target(path: str):
    if path == "/":
        return ("redirect", None)
    for prefix, target in ROUTES:
        if path == prefix or path.startswith(prefix + "/"):
            return ("proxy", target)
    return (None, None)


class ProxyHandler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def do_GET(self):
        self._handle()

    def do_HEAD(self):
        self._handle()

    def do_POST(self):
        self._handle()

    def do_PUT(self):
        self._handle()

    def do_PATCH(self):
        self._handle()

    def do_DELETE(self):
        self._handle()

    def _handle(self):
        action, target = resolve_target(self.path)

        if action == "redirect":
            self.send_response(302)
            self.send_header("Location", "/frontend/")
            self.send_header("Content-Length", "0")
            self.end_headers()
            return

        if action != "proxy":
            body = b"Not Found"
            self.send_response(404)
            self.send_header("Content-Type", "text/plain; charset=utf-8")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            if self.command != "HEAD":
                self.wfile.write(body)
            return

        host, port = target
        content_length = int(self.headers.get("Content-Length", "0"))
        request_body = self.rfile.read(content_length) if content_length else None

        upstream_headers = {
            key: value
            for key, value in self.headers.items()
            if key.lower() not in {"host", "connection"}
        }
        upstream_headers["Host"] = f"{host}:{port}"
        upstream_headers["Connection"] = "close"

        conn = http.client.HTTPConnection(host, port, timeout=30)
        try:
            conn.request(self.command, self.path, body=request_body, headers=upstream_headers)
            resp = conn.getresponse()
            response_body = resp.read()

            self.send_response(resp.status, resp.reason)
            for key, value in resp.getheaders():
                lower_key = key.lower()
                if lower_key in {"connection", "keep-alive", "proxy-authenticate", "proxy-authorization", "te", "trailers", "transfer-encoding", "upgrade"}:
                    continue
                if lower_key == "location" and value.startswith("http://127.0.0.1:18080"):
                    value = value.replace("http://127.0.0.1:18080", "")
                self.send_header(key, value)
            self.send_header("Content-Length", str(len(response_body)))
            self.end_headers()

            if self.command != "HEAD":
                self.wfile.write(response_body)
        except Exception as exc:
            body = f"Bad Gateway: {exc}".encode("utf-8")
            self.send_response(502)
            self.send_header("Content-Type", "text/plain; charset=utf-8")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            if self.command != "HEAD":
                self.wfile.write(body)
        finally:
            conn.close()


if __name__ == "__main__":
    server = ThreadingHTTPServer(("127.0.0.1", 18090), ProxyHandler)
    print("Local proxy listening on http://127.0.0.1:18090")
    server.serve_forever()
