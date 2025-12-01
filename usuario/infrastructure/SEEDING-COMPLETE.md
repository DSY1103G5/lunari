# Database Seeding Complete ✅

## Summary

Successfully populated the DynamoDB `lunari-users-dev` table with 4 sample e-commerce clients!

---

## What Was Done

### 1. ✅ Updated DynamoDB Table Schema

**Changes made to `dynamodb-table.yaml`**:
- **Partition Key**: `userId` → `id`
- **Removed GSIs**: `RoleActiveIndex` (roles removed from client model)
- **Added GSI**: `UsernameIndex` (for username-based lookups)
- **Kept GSI**: `EmailIndex` (for email-based authentication)

### 2. ✅ Created Seed Data Files

**Files created**:
- `infrastructure/seed-data.json` - 4 sample clients with full e-commerce data
- `infrastructure/seed-database.py` - Python script to populate DynamoDB
- `infrastructure/seed-database.js` - Node.js alternative script
- `infrastructure/seed-database.sh` - Bash script (requires jq)

### 3. ✅ Populated Database

**Users inserted** (4 total):
1. **omunoz** (osca.munozs@duocuc.cl)
   - Level: Gold
   - Points: 7,500
   - Purchases: 24
   - Coupons: 2

2. **amillan** (ang.millan@duocuc.cl)
   - Level: Platinum
   - Points: 15,420
   - Purchases: 42
   - Coupons: 3
   - **Pro gamer** with streaming platforms

3. **obadilla** (obadilla@duoc.cl)
   - Level: Silver
   - Points: 3,200
   - Purchases: 15
   - Coupons: 2
   - Retro gaming enthusiast

4. **estudiante_duoc** (pedro.duoc@duoc.cl)
   - Level: Bronze
   - Points: 850
   - Purchases: 5
   - Coupons: 1
   - **Not verified** (for testing verification flows)

---

## Data Schema

Each user includes:

```json
{
  "id": "1",
  "username": "omunoz",
  "email": "osca.munozs@duocuc.cl",
  "password": "12345678",
  "isActive": true,
  "isVerified": true,
  "personal": {
    "firstName": "Oscar",
    "lastName": "Munoz",
    "phone": "+56 9 1234 5678",
    "birthdate": "1993-04-27",
    "bio": "...",
    "avatar": null,
    "memberSince": "2022"
  },
  "address": {
    "addressLine1": "Av. Providencia 1234",
    "addressLine2": "Departamento 56",
    "city": "Santiago",
    "region": "Región Metropolitana",
    "postalCode": "7500000",
    "country": "chile",
    "deliveryNotes": "..."
  },
  "preferences": {
    "favoriteCategories": ["JM", "CG", "AC"],
    "preferredPlatform": "pc",
    "gamingHours": "16-30",
    "notifyOffers": true,
    "notifyNewProducts": true,
    "notifyRestocks": false,
    "notifyNewsletter": true
  },
  "gaming": {
    "gamerTag": "OMunoz93",
    "favoriteGenre": "rpg",
    "skillLevel": "advanced",
    "streamingPlatforms": ["twitch", "youtube"],
    "favoriteGames": "..."
  },
  "stats": {
    "level": "Gold",
    "points": 7500,
    "purchases": 24,
    "reviews": 12,
    "favorites": 8
  },
  "coupons": [
    {
      "id": "COUP-001",
      "code": "BRONZE-ALX-001",
      "description": "Cupón Bronze - $1.000 de descuento",
      "type": "fixed",
      "value": 1000,
      "minPurchase": 0,
      "expiresAt": "2025-12-31",
      "isUsed": false
    }
  ]
}
```

---

## Testing the Data

### Via AWS CLI

```bash
# Count total users
aws dynamodb scan \
  --table-name lunari-users-dev \
  --region us-east-1 \
  --select COUNT

# Get all users
aws dynamodb scan \
  --table-name lunari-users-dev \
  --region us-east-1

# Get user by ID
aws dynamodb get-item \
  --table-name lunari-users-dev \
  --region us-east-1 \
  --key '{"id": {"S": "1"}}'

# Query by email (using EmailIndex GSI)
aws dynamodb query \
  --table-name lunari-users-dev \
  --region us-east-1 \
  --index-name EmailIndex \
  --key-condition-expression "email = :email" \
  --expression-attribute-values '{":email": {"S": "osca.munozs@duocuc.cl"}}'

# Query by username (using UsernameIndex GSI)
aws dynamodb query \
  --table-name lunari-users-dev \
  --region us-east-1 \
  --index-name UsernameIndex \
  --key-condition-expression "username = :username" \
  --expression-attribute-values '{":username": {"S": "omunoz"}}'
```

### Via API (Once Deployed)

```bash
# Get all clients
curl http://your-api:8080/api/v1/users

# Get specific client
curl http://your-api:8080/api/v1/users/1

# Get client by email (login)
curl http://your-api:8080/api/v1/users/email/osca.munozs@duocuc.cl
```

---

## Re-seeding the Database

If you need to repopulate the database later:

```bash
cd infrastructure

# Using Python (recommended)
python3 seed-database.py dev

# Using Node.js (requires npm install)
node seed-database.js dev

# Using Bash (requires jq)
./seed-database.sh dev
```

---

## Adding More Users

To add more users:

1. Edit `infrastructure/seed-data.json`
2. Add your user data following the schema
3. Run the seed script again:
   ```bash
   python3 infrastructure/seed-database.py dev
   ```

**Note**: The script uses `PutItem` which will **overwrite** existing users with the same `id`. To avoid duplicates, use unique IDs.

---

## Sample Queries for Testing

### Test Authentication
```bash
# Login with omunoz
# Email: osca.munozs@duocuc.cl
# Password: 12345678

# Login with amillan (Pro gamer)
# Email: ang.millan@duocuc.cl
# Password: demo123
```

### Test User States
- **Active & Verified**: omunoz, amillan, obadilla
- **Active but Not Verified**: estudiante_duoc (test verification flow)

### Test Loyalty Levels
- **Bronze**: estudiante_duoc (850 points)
- **Silver**: obadilla (3,200 points)
- **Gold**: omunoz (7,500 points)
- **Platinum**: amillan (15,420 points)

### Test Coupons
- **omunoz**: Has 1 active coupon (BRONZE-ALX-001) and 1 used coupon
- **amillan**: Has 2 active coupons (GOLD and PLATINUM)
- **obadilla**: Has 2 active coupons (BRONZE and SILVER)
- **estudiante_duoc**: Has 1 active coupon (BRONZE)

### Test Gaming Profiles
- **omunoz**: Advanced RPG player, PC gamer, streams on Twitch/YouTube
- **amillan**: Pro FPS player, console gamer, streams on Twitch/YouTube/Kick
- **obadilla**: Intermediate adventure gamer, both platforms, streams on YouTube
- **estudiante_duoc**: Intermediate FPS player, PC gamer, streams on Twitch

---

## Next Steps

1. ✅ **DynamoDB populated** with sample data
2. ✅ **Table schema updated** (id, email, username indexes)
3. 🔄 **Deploy API** to EC2 to test endpoints
4. 🔄 **Test CRUD operations** via Swagger UI
5. 🔄 **Test authentication** with sample credentials
6. 🔄 **Test coupon management** endpoints
7. 🔄 **Test gaming profile** updates

---

## Files Created

1. `infrastructure/seed-data.json` - Sample client data
2. `infrastructure/seed-database.py` - Python seed script ⭐ (recommended)
3. `infrastructure/seed-database.js` - Node.js seed script
4. `infrastructure/seed-database.sh` - Bash seed script
5. `infrastructure/SEEDING-COMPLETE.md` - This documentation

---

**Database ready for testing!** 🎮🛒
