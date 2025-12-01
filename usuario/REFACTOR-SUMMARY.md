# Usuario Service Refactor Summary

## ✅ Completed Changes

### 1. New Model Classes Created
- ✅ `Personal.java` - Personal information (firstName, lastName, phone, birthdate, bio, avatar, memberSince)
- ✅ `Gaming.java` - Gaming profile (gamerTag, favoriteGenre, skillLevel, streamingPlatforms, favoriteGames)
- ✅ `ClientPreferences.java` - Client preferences (favoriteCategories, preferredPlatform, gamingHours, notifications)
- ✅ `ClientStats.java` - Client statistics (level, points, purchases, reviews, favorites)
- ✅ `Coupon.java` - Coupon model (id, code, description, type, value, minPurchase, expiresAt, isUsed)

### 2. Updated Models
- ✅ `Address.java` - Now has addressLine1, addressLine2, deliveryNotes
- ✅ `User.java` - Refactored to e-commerce client structure with nested objects
- ✅ `UserRepresentation.java` - DTO updated to match new structure
- ✅ `UserMapper.java` - Mapper updated for new schema

### 3. Key Schema Changes

#### Before (Old Structure)
```json
{
  "userId": "uuid",
  "firstName": "Oscar",
  "lastName": "Munoz",
  "email": "...",
  "phone": "...",
  "level": 5,
  "points": 1500
}
```

#### After (New E-Commerce Structure)
```json
{
  "id": "1",
  "username": "omunoz",
  "email": "osca.munozs@duocuc.cl",
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
    "favoriteGames": "The Witcher 3, Civilization VI..."
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

## 🔧 Pending Changes

### 1. Service Layer (`UserServiceImpl.java`)

The service needs to be updated to work with the new nested structure. Key changes:

#### Field Access Changes
```java
// ❌ Old way
user.setFirstName("Oscar");
user.setLastName("Munoz");
user.setLevel(5);
user.setPoints(1000L);

// ✅ New way
Personal personal = new Personal();
personal.setFirstName("Oscar");
personal.setLastName("Munoz");
user.setPersonal(personal);

ClientStats stats = new ClientStats();
stats.setLevel("Gold");
stats.setPoints(7500L);
user.setStats(stats);
```

#### ID Field Changes
```java
// ❌ Old
user.setUserId(uuid);
String id = user.getUserId();

// ✅ New
user.setId(id);
String id = user.getId();
```

#### Removed Fields (No longer needed for e-commerce clients)
- ❌ `role` and `EmbeddedRole` (clients don't have roles)
- ❌ `metadata`
- ❌ `lastLogin`, `tokenVerification`, `tokenExpiration` (move to separate auth service)
- ❌ `memberSince` at root level (now in `personal`)

### 2. Controller Layer (`UserController.java`)

Update endpoints to be more e-commerce focused:

#### Endpoint Naming
```java
// ❌ Old (admin/portal focused)
@GetMapping("/users")
@GetMapping("/users/{userId}")
@PutMapping("/users/{userId}/role")

// ✅ New (e-commerce client focused)
@GetMapping("/clients")
@GetMapping("/clients/{id}")
@GetMapping("/clients/{id}/profile")
@PutMapping("/clients/{id}/preferences")
@GetMapping("/clients/{id}/coupons")
@PostMapping("/clients/{id}/coupons/{couponId}/use")
```

### 3. HATEOAS Assemblers

Update `UserModelAssembler.java` to generate e-commerce-relevant links:

```java
// ✅ E-commerce relevant links
representation.add(linkTo(methodOn(UserController.class)
    .getClientById(user.getId())).withSelfRel());

representation.add(linkTo(methodOn(UserController.class)
    .getClientCoupons(user.getId())).withRel("coupons"));

representation.add(linkTo(methodOn(UserController.class)
    .getClientOrders(user.getId())).withRel("orders"));

representation.add(linkTo(methodOn(UserController.class)
    .getClientReviews(user.getId())).withRel("reviews"));

// Remove admin links like "activate", "deactivate", "verify"
```

### 4. DynamoDB Table Update

The DynamoDB table needs a new GSI for username lookups:

```yaml
# Add to infrastructure/dynamodb-table.yaml
GlobalSecondaryIndexes:
  - IndexName: UsernameIndex
    KeySchema:
      - AttributeName: username
        KeyType: HASH
    Projection:
      ProjectionType: ALL
```

### 5. Repository Layer

The repository should work without changes since DynamoDB Enhanced Client handles the new structure automatically. Just verify queries still work.

---

## 🚀 Quick Fix Option

If you want to get it compiling quickly, you have two options:

### Option A: Keep Both Structures (Backward Compatible)
Add both old and new fields to User model temporarily, mark old ones as `@Deprecated`.

### Option B: Complete Refactor (Recommended)
1. Update `UserServiceImpl.java` to use nested objects
2. Simplify controller endpoints for e-commerce
3. Remove role/admin functionality
4. Add client-specific endpoints (coupons, preferences)
5. Update DynamoDB table with UsernameIndex

---

## 📝 Next Steps

1. **Fix Service Layer** - Update UserServiceImpl.java to work with nested objects
2. **Simplify Controllers** - Remove admin endpoints, add client-focused ones
3. **Update DynamoDB** - Add UsernameIndex GSI
4. **Add New Endpoints**:
   - `GET /api/v1/clients/{id}/coupons`
   - `POST /api/v1/clients/{id}/coupons/{couponId}/use`
   - `PUT /api/v1/clients/{id}/preferences`
   - `PUT /api/v1/clients/{id}/gaming`
   - `GET /api/v1/clients/{id}/stats`
5. **Test with Sample Data** - Create a test client with the new schema
6. **Rebuild and Deploy**

---

## 📦 Deployment Notes

When deploying the refactored service:

1. **DynamoDB Migration**: Existing data will need migration to new structure
2. **Breaking Changes**: This is a breaking API change - version it as v2 if needed
3. **Client Updates**: Frontend and other services need to update their API calls

---

## Need Help?

The model layer is done. The main work remaining is updating the service and controller layers to use the new nested object structure instead of flat fields.

Would you like me to:
1. Complete the service refactor automatically?
2. Create a migration script for existing data?
3. Just fix compilation errors to get it building?
