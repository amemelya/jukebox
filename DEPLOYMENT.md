# Jukebox EC2 + RDS Deployment

This project is already wired for a production PostgreSQL database through Spring profile `production`.

The missing pieces are:

1. Run the backend on the EC2 instance.
2. Point it at the existing AWS RDS PostgreSQL instance.
3. Serve the frontend from the EC2 public IP.
4. Reverse proxy `/api` to the backend so the frontend no longer depends on `localhost`.

## Current AWS References

From `AWS_Postgres_Setup_Reference.rtf`:

- Region: `eu-north-1`
- RDS host: `jukebox-db.cbgmumkuafkd.eu-north-1.rds.amazonaws.com`
- DB name: `jukebox`
- DB user: `jukebox_app`
- EC2 instance id: `i-01e955ee3d006aa30`

## Recommended Architecture

- Nginx on EC2 serves the built frontend on port `80`.
- Nginx proxies `/api/*` to Spring Boot on `127.0.0.1:4000`.
- Spring Boot runs as a `systemd` service.
- Spring Boot connects to RDS PostgreSQL using the production profile.

This is the right shape because:

- the browser talks to one origin
- CORS becomes simple
- the backend does not need to be directly exposed to the internet

## 1. EC2 Security Group

Inbound rules on the EC2 security group should allow:

- `22` from your IP only
- `80` from `0.0.0.0/0`
- `443` from `0.0.0.0/0` later when you add HTTPS

Do not expose backend port `4000` publicly if Nginx is proxying requests.

## 2. RDS Security Group

Your notes already show the correct pattern:

- allow PostgreSQL `5432`
- source should be the EC2 security group, not the whole internet

## 3. Backend Environment on EC2

Create `/opt/jukebox/backend/.env`:

```bash
SPRING_PROFILES_ACTIVE=production
APP_CLIENT_ORIGIN=http://YOUR_EC2_PUBLIC_IP
DB_HOST=jukebox-db.cbgmumkuafkd.eu-north-1.rds.amazonaws.com
DB_PORT=5432
DB_NAME=jukebox
DB_USERNAME=jukebox_app
DB_PASSWORD=YOUR_DB_PASSWORD
APP_JWT_SECRET=REPLACE_WITH_A_LONG_RANDOM_SECRET
APP_ADMIN_SECRET=REPLACE_WITH_A_LONG_RANDOM_SECRET
APP_SMS_PROVIDER=mock
APP_EXPOSE_DEV_OTP=false
GOOGLE_CLIENT_ID=YOUR_GOOGLE_CLIENT_ID
```

If Google sign-in is not being used yet, `GOOGLE_CLIENT_ID` can stay empty.

## 4. Build and Run the Backend on EC2

Install Java and Maven if needed:

```bash
sudo dnf install java-1.8.0-amazon-corretto-devel maven -y
```

Build:

```bash
cd /opt/jukebox/backend
mvn clean package -DskipTests
```

Expected jar:

```bash
target/backend-0.0.1-SNAPSHOT.jar
```

## 5. Create the Backend Service

Create `/etc/systemd/system/jukebox-backend.service`:

```ini
[Unit]
Description=Jukebox Backend
After=network.target

[Service]
User=ec2-user
WorkingDirectory=/opt/jukebox/backend
EnvironmentFile=/opt/jukebox/backend/.env
ExecStart=/usr/bin/java -jar /opt/jukebox/backend/target/backend-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Start it:

```bash
sudo systemctl daemon-reload
sudo systemctl enable jukebox-backend
sudo systemctl start jukebox-backend
sudo systemctl status jukebox-backend
```

Check logs:

```bash
journalctl -u jukebox-backend -f
```

## 6. Build the Frontend for the Public IP

The frontend now defaults to `/api/auth`, which is correct for Nginx reverse proxy deployment.

Build:

```bash
cd /opt/jukebox/frontend
npm ci
npm run build
```

Expected output:

```bash
dist/
```

## 7. Install and Configure Nginx

Install:

```bash
sudo dnf install nginx -y
```

Create `/etc/nginx/conf.d/jukebox.conf`:

```nginx
server {
    listen 80;
    server_name _;

    root /opt/jukebox/frontend/dist;
    index index.html;

    location / {
        try_files $uri /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:4000/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Start Nginx:

```bash
sudo nginx -t
sudo systemctl enable nginx
sudo systemctl restart nginx
```

## 8. What URL to Use

After the steps above, use:

```text
http://YOUR_EC2_PUBLIC_IP
```

The browser should not call `localhost` anymore.

API calls should go to:

```text
http://YOUR_EC2_PUBLIC_IP/api/auth
```

through the reverse proxy.

## 9. Important Google OAuth Note

If you use Google login, update the Google OAuth configuration to include the exact deployed origin:

- `http://YOUR_EC2_PUBLIC_IP`

Later, when HTTPS is added, update it again to:

- `https://YOUR_DOMAIN`

## 10. Recommended Next Step

Do not stop at a raw public IP deployment.

The correct follow-up is:

1. attach an Elastic IP
2. point a real domain to it
3. add HTTPS with Nginx and Let's Encrypt

Running auth over plain HTTP on a bare public IP is acceptable only as a short-term test environment.
