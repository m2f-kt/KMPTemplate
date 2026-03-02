# Getting Started

Step-by-step guide to go from a fresh clone to a fully running development environment.

## Prerequisites

- [ ] **JDK 11+** — Check with `java -version`. [Download](https://adoptium.net/)
- [ ] **Docker Desktop** — Must be running. [Download](https://docs.docker.com/get-docker/)
- [ ] **Git** — Check with `git --version`

## Setup Steps

### Step 1: Clone the repository

```bash
git clone <repo-url>
cd template
```

### Step 2: Configure environment (optional)

Defaults work for local development — this step is optional.

```bash
cp .env.example .env
# Edit .env to customize settings (e.g., OAuth keys, AI API key)
```

### Step 3: Run setup

This checks prerequisites and starts all Docker services (PostgreSQL, MinIO, MailHog):

```bash
./gradlew devSetup
```

Wait for all containers to report healthy. This typically takes 15–30 seconds.

### Step 4: Seed demo data

```bash
./gradlew seedData
```

This creates a demo user:
- **Email:** `dev@example.com`
- **Password:** `password`

### Step 5: Start the server

```bash
./gradlew :server:run
```

The server starts on `http://localhost:8080`. Keep this terminal running.

### Step 6: Verify everything works

In a new terminal:

```bash
./gradlew verifySetup
```

Or manually check:

```bash
# Health endpoint (should return JSON with all services "up")
curl http://localhost:8080/health

# Check Docker containers
docker ps
```

You can also visit:
- **MailHog Web UI:** [http://localhost:8025](http://localhost:8025) — See all sent emails
- **MinIO Console:** [http://localhost:9003](http://localhost:9003) — Login with `minioadmin`/`minioadmin`

### Step 7: Start the web app

In a new terminal:

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

This opens the WASM web app in your browser.

### Step 8: Login

Use the demo credentials:
- **Email:** `dev@example.com`
- **Password:** `password`

You should see the dashboard. You're all set!

## Troubleshooting

### Port already in use

**Problem:** `./gradlew devSetup` fails with port conflict.

**Cause:** Another process is using a required port (5436, 9002, 9003, 1025, 8025, or 8080).

**Fix:**
```bash
# Find and kill the process using the port
lsof -ti:PORT | xargs kill

# Or check which ports are in use
./gradlew checkSetup
```

### Docker containers won't start

**Problem:** `docker compose up` fails or containers exit immediately.

**Cause:** Docker Desktop is not running, or insufficient resources.

**Fix:**
1. Start Docker Desktop
2. Ensure Docker has at least 4GB RAM allocated
3. Retry: `./gradlew devUp`

### Database connection refused

**Problem:** Server fails to connect to PostgreSQL.

**Cause:** PostgreSQL container is not healthy yet.

**Fix:**
```bash
# Check container health
docker inspect --format '{{.State.Health.Status}}' template-postgres

# View logs for errors
docker compose logs postgres

# Restart if needed
docker compose restart postgres
```

### Gradle build fails

**Problem:** Compilation errors or unresolved dependencies.

**Cause:** Wrong JDK version or stale Gradle cache.

**Fix:**
```bash
# Check JDK version (must be 11+)
java -version

# Clean and rebuild
./gradlew clean build
```

### Server won't start

**Problem:** Server crashes on startup.

**Cause:** Usually missing database migrations or Docker services not running.

**Fix:**
1. Ensure Docker services are healthy: `./gradlew verifySetup`
2. Migrations run automatically on server start — check logs for migration errors
3. If database is corrupted, reset: `docker compose down -v && ./gradlew devUp`

### MinIO bucket missing

**Problem:** File uploads fail with "bucket not found".

**Cause:** The `minio-init` container didn't run successfully.

**Fix:**
```bash
docker compose up minio-init
```

### Emails not appearing in MailHog

**Problem:** Password reset or invitation emails don't show up.

**Cause:** MailHog container is not running.

**Fix:**
```bash
# Check if MailHog is running
docker ps | grep mailhog

# Restart if needed
docker compose up -d mailhog

# Visit MailHog web UI
open http://localhost:8025
```

### Complete reset

If all else fails, reset everything and start fresh:

```bash
docker compose down -v   # Stop containers and delete volumes
./gradlew devSetup       # Recreate everything
./gradlew seedData       # Re-seed demo data
./gradlew :server:run    # Start server
```
