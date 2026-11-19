#!/usr/bin/env python3
import http.server
import socketserver
import socket
import os

PORT = 3000

class Handler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        super().end_headers()

    def do_GET(self):
        clean_path = self.path.split('?')[0].split('#')[0]
        if clean_path in ('/hyper_alhayat.apk', '/app.apk', '/download'):
            target = '/hyper_alhayat.apk' if os.path.exists('/hyper_alhayat.apk') else 'hyper_alhayat.apk'
            if os.path.exists(target):
                self.send_response(200)
                self.send_header('Content-Type', 'application/vnd.android.package-archive')
                self.send_header('Content-Disposition', 'attachment; filename="hyper_alhayat.apk"')
                self.send_header('Content-Length', str(os.path.getsize(target)))
                self.end_headers()
                with open(target, 'rb') as f:
                    self.wfile.write(f.read())
                return
        elif clean_path in ('/hyper_alhayat_website.zip', '/website.zip', '/site.zip'):
            target = 'hyper_alhayat_website.zip'
            if os.path.exists(target):
                self.send_response(200)
                self.send_header('Content-Type', 'application/zip')
                self.send_header('Content-Disposition', 'attachment; filename="hyper_alhayat_website.zip"')
                self.send_header('Content-Length', str(os.path.getsize(target)))
                self.end_headers()
                with open(target, 'rb') as f:
                    self.wfile.write(f.read())
                return
        elif clean_path in ('/download-html',):
            target = '/app/applet/web/index.html' if os.path.exists('/app/applet/web/index.html') else 'web/index.html'
            if os.path.exists(target):
                self.send_response(200)
                self.send_header('Content-Type', 'text/html; charset=utf-8')
                self.send_header('Content-Disposition', 'attachment; filename="index.html"')
                self.send_header('Content-Length', str(os.path.getsize(target)))
                self.end_headers()
                with open(target, 'rb') as f:
                    self.wfile.write(f.read())
                return
        # Serve Web App for root and standard web routes
        if clean_path in ('/', '/index.html', '/admin', '/shop', '/orders'):
            web_index = os.path.join(os.getcwd(), 'web', 'index.html')
            if not os.path.exists(web_index):
                web_index = '/web/index.html'
            if os.path.exists(web_index):
                self.send_response(200)
                self.send_header('Content-Type', 'text/html; charset=utf-8')
                with open(web_index, 'rb') as f:
                    content = f.read()
                self.send_header('Content-Length', str(len(content)))
                self.end_headers()
                self.wfile.write(content)
                return
            else:
                self.send_response(404)
                self.end_headers()
                self.wfile.write(b'Web app not found')
                return
        super().do_GET()

os.chdir('/app/applet')
class ReusableTCPServer(socketserver.TCPServer):
    allow_reuse_address = True
    def server_bind(self):
        try:
            self.socket.setsockopt(socket.SOL_SOCKET, 15, 1) # SO_REUSEPORT
        except Exception:
            pass
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        super().server_bind()

with ReusableTCPServer(("", PORT), Handler) as httpd:
    print(f"Serving HTTP on port {PORT}...")
    httpd.serve_forever()
