#!/usr/bin/env python3
import http.server
import socketserver
import os

PORT = 3000

class Handler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        super().end_headers()

    def do_GET(self):
        if self.path in ('/hyper_alhayat.apk', '/app.apk', '/download'):
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
        elif self.path == '/' or self.path == '/index.html':
            self.send_response(200)
            self.send_header('Content-Type', 'text/html; charset=utf-8')
            self.end_headers()
            html = """<!DOCTYPE html>
<html dir="rtl" lang="ar">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>تحميل تطبيق هايبر الحياة</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f3f4f6; text-align: center; padding: 30px 15px; margin: 0; }
        .card { background: white; max-width: 420px; margin: 0 auto; padding: 25px; border-radius: 20px; box-shadow: 0 4px 15px rgba(0,0,0,0.08); }
        .btn { display: inline-block; background: #e11d48; color: white; padding: 14px 28px; border-radius: 14px; text-decoration: none; font-weight: bold; font-size: 16px; margin-top: 15px; }
        .badge { background: #fee2e2; color: #991b1b; padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; display: inline-block; }
    </style>
</head>
<body>
    <div class="card">
        <span class="badge">الإصدار الأحدث المطور ⚡</span>
        <h1 style="color: #1e293b; margin: 15px 0 8px;">🛒 هايبر الحياة</h1>
        <p style="color: #64748b; font-size: 14px; margin-bottom: 20px;">مدينة بدر - الإسكان الاجتماعي (فرع الياسمين & القرنفل)</p>
        <p style="color: #334155; font-size: 14px; line-height: 1.6;">تم تحديث التطبيق بالكامل مع عروض التوفير، تحديد العنوان بالـ GPS بنقرة، إعادة طلب الفاتورة السابقة بضغطة زر، طباعة البون الحراري، وتقارير المبيعات وحالة فتح وإغلاق الهايبر!</p>
        <a class="btn" href="/hyper_alhayat.apk">📥 تحميل تطبيق هايبر الحياة (APK)</a>
    </div>
</body>
</html>"""
            self.wfile.write(html.encode('utf-8'))
            return
        super().do_GET()

os.chdir('/app/applet')
with socketserver.TCPServer(("", PORT), Handler) as httpd:
    httpd.allow_reuse_address = True
    print(f"Serving HTTP on port {PORT}...")
    httpd.serve_forever()
