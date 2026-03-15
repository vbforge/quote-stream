# Deployment Guide

## The full pipeline at a glance

```
Your machine  →  git push main  →  GitHub Actions
                                        │
                              ┌─────────┴──────────┐
                              │  1. mvn package     │
                              │  2. run tests       │
                              │  3. docker build    │
                              │  4. docker push     │
                              └─────────┬──────────┘
                                        │
                                  DockerHub
                                  your-name/quote-stream:latest
                                        │
                                  Your server
                              docker-compose pull
                              docker-compose up -d
```

---

## Step 1 — GitHub Secrets setup

The CI workflow needs two secrets to push to DockerHub.

1. Go to your GitHub repo → **Settings → Secrets and variables → Actions**
2. Add these two secrets:

| Secret name          | Value                                        |
|----------------------|----------------------------------------------|
| `DOCKERHUB_USERNAME` | Your DockerHub username                      |
| `DOCKERHUB_TOKEN`    | A DockerHub Access Token (not your password) |

**To create a DockerHub Access Token:**
DockerHub → Account Settings → Security → New Access Token
Name it `github-actions`, permission: Read & Write

---

## Step 2 — First push

```bash
git add .
git commit -m "initial commit"
git push origin main
```

Go to **Actions** tab on GitHub — you will see the pipeline running.
First run takes ~3 minutes (Maven downloads dependencies).
Subsequent runs take ~1 minute (GitHub caches Maven deps and Docker layers).

---

## Step 3 — Deploy to a server

### Option A — Railway (easiest free tier)

1. Railway dashboard → New Project → Deploy from GitHub repo
2. Add a PostgreSQL plugin — Railway gives you the connection string
3. Set environment variables in Railway dashboard:
   ```
   DB_URL      = (copy from Railway PostgreSQL plugin)
   DB_USER     = (from Railway PostgreSQL)
   DB_PASSWORD = (from Railway PostgreSQL)
   ```
4. Railway auto-deploys on every push to main — nothing else needed.

### Option B — Any VPS (Render, Fly.io, DigitalOcean, Hetzner, etc.)

SSH into your server, then:

```bash
# 1. Install Docker if not present
curl -fsSL https://get.docker.com | sh

# 2. Create app directory
mkdir ~/quote-stream && cd ~/quote-stream

# 3. Copy docker-compose.prod.yml here (from your repo or paste it)

# 4. Create your .env file from the example
nano .env
# Fill in:
#   DOCKERHUB_USERNAME=yourname
#   DB_USER=quotestream
#   DB_PASSWORD=something_strong_and_random

# 5. Pull image and start
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d

# 6. Check it is running
docker compose -f docker-compose.prod.yml logs app
```

App is now live at http://your-server-ip:8080

### Updating after a new push to main

GitHub Actions pushes a new image automatically.
To pull it on your server:

```bash
cd ~/quote-stream
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

---

## Free hosting options compared

| Platform     | App free tier     | DB free tier           | Notes                            |
|--------------|-------------------|------------------------|----------------------------------|
| Railway      | $5 credits/month  | Included               | Best DX, auto-deploy from GitHub |
| Render       | Free (spins down) | PostgreSQL free 90 days| Use Supabase for DB instead      |
| Fly.io       | Free (256MB RAM)  | Free small Postgres    | Requires credit card             |
| Supabase     | —                 | Free forever (500MB)   | Use as DB with any of the above  |

**Recommended combo:** Railway for app + built-in PostgreSQL (simplest)
**Alternative:** Render for app + Supabase for DB (DB free forever)

### Using Supabase as DB

1. Create project at supabase.com
2. Settings → Database → Connection string → URI mode
3. Set env vars:
   ```
   DB_URL      = jdbc:postgresql://db.xxxx.supabase.co:5432/postgres
   DB_USER     = postgres
   DB_PASSWORD = your-supabase-password
   ```

---

## Environment variables reference

| Variable             | Where used     | Description                     |
|----------------------|----------------|---------------------------------|
| `DB_URL`             | App server     | Full JDBC connection string      |
| `DB_USER`            | App server     | Database username                |
| `DB_PASSWORD`        | App + DB       | Database password                |
| `DOCKERHUB_USERNAME` | GitHub Secrets | For CI to push images            |
| `DOCKERHUB_TOKEN`    | GitHub Secrets | DockerHub access token (not pw)  |

---

## Useful server commands

```bash
# Live logs
docker compose -f docker-compose.prod.yml logs -f app

# Restart app after config change
docker compose -f docker-compose.prod.yml restart app

# Stop everything
docker compose -f docker-compose.prod.yml down

# Stop and wipe DB volume (destructive!)
docker compose -f docker-compose.prod.yml down -v
```
