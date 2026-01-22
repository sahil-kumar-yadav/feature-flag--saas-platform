# Feature Flag Platform

A production-ready feature flag management system built with Spring Boot, inspired by LaunchDarkly.

## 🎯 Project Overview

This platform allows teams to enable/disable features without redeploying code, target users dynamically, perform percentage-based rollouts, and maintain comprehensive audit history across multiple environments.

## 🏗️ High-Level Architecture

### Components

1. **API Layer** (Controllers)
   - Admin APIs for flag management
   - SDK-style evaluation endpoint
   - Audit log access

2. **Service Layer**
   - FeatureFlagService: Core business logic
   - Evaluation logic with caching

3. **Data Layer**
   - PostgreSQL for persistence
   - Redis for caching
   - JPA repositories

4. **Security**
   - JWT-based authentication (placeholder)
   - Role-based access control (RBAC)

### Data Flow

```
Client App → Evaluation API → Redis Cache → Database → Response
Admin UI → Admin APIs → Service → Database + Audit Logs
```

## 📊 Database Schema

### Tables

#### feature_flags
- `id` (BIGINT, PK)
- `key` (VARCHAR, UNIQUE)
- `name` (VARCHAR)
- `description` (TEXT)
- `type` (ENUM: BOOLEAN, PERCENTAGE, USER_TARGETED)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)
- `created_by` (VARCHAR)

#### flag_values
- `id` (BIGINT, PK)
- `flag_id` (BIGINT, FK)
- `environment` (ENUM: DEV, STAGING, PROD)
- `enabled` (BOOLEAN) - for BOOLEAN type
- `percentage` (INTEGER) - for PERCENTAGE type
- `user_ids` (TEXT) - comma-separated for USER_TARGETED
- `updated_at` (TIMESTAMP)
- `updated_by` (VARCHAR)

#### audit_logs
- `id` (BIGINT, PK)
- `flag_id` (BIGINT, FK)
- `environment` (ENUM)
- `action` (VARCHAR: CREATE, UPDATE, DELETE)
- `old_value` (TEXT)
- `new_value` (TEXT)
- `changed_by` (VARCHAR)
- `timestamp` (TIMESTAMP)

## 🔧 Domain Models & Enums

### Enums
- `FlagType`: BOOLEAN, PERCENTAGE, USER_TARGETED
- `Environment`: DEV, STAGING, PROD

### Entities
- `FeatureFlag`: Core flag metadata
- `FlagValue`: Environment-specific values
- `AuditLog`: Change history

## 🌐 REST API Design

### Admin APIs

#### Create Flag
```
POST /api/flags
{
  "key": "new-feature",
  "name": "New Feature",
  "description": "A new feature",
  "type": "BOOLEAN",
  "createdBy": "admin"
}
```

#### Update Flag Value
```
PUT /api/flags/{flagId}/environments/{environment}
{
  "value": true,
  "updatedBy": "admin"
}
```

#### Get Audit Logs
```
GET /api/flags/{flagId}/audit
```

### Evaluation API (SDK-style)

#### Evaluate Flag
```
GET /api/evaluate/{key}?environment=PROD&userId=user123
Response: {"enabled": true}
```

## ⚡ Feature Evaluation Algorithm

### Logic Flow

1. **Cache Check**: Query Redis first
2. **Database Lookup**: If cache miss, fetch from DB
3. **Type-specific Evaluation**:
   - **BOOLEAN**: Return `enabled` value
   - **PERCENTAGE**: Hash userId % 100 < percentage
   - **USER_TARGETED**: Check if userId in comma-separated list
4. **Cache Result**: Store in Redis for 5 minutes

### Edge Cases Handled

- Invalid flag key → Default to false
- Missing environment config → Default to false
- Null values → Appropriate defaults
- User ID hashing for percentage rollouts

## 🚀 Caching Strategy (Redis)

### Cache Keys
- `flag:{key}:{environment}` → Boolean result
- TTL: 5 minutes
- Invalidation: On flag value updates

### Benefits
- Sub-millisecond evaluation latency
- Reduced database load
- High throughput for SDK calls

## 🔐 Security Design

### Current Implementation
- Public evaluation endpoint (no auth required)
- Admin endpoints require authentication (placeholder)
- JWT token support (framework in place)

### Future Enhancements
- User authentication with JWT
- RBAC for admin operations
- API key authentication for SDK

## 📈 Scalability Considerations

### Database
- PostgreSQL with proper indexing
- Connection pooling via HikariCP
- Read replicas for audit logs (future)

### Caching
- Redis cluster for horizontal scaling
- Cache warming strategies
- Circuit breaker for Redis failures

### Application
- Stateless design for horizontal scaling
- Async audit logging
- Rate limiting for admin APIs

## ⚠️ Common Pitfalls & Best Practices

### Pitfalls to Avoid
1. **Cache Inconsistency**: Always invalidate cache on updates
2. **Race Conditions**: Use database transactions for updates
3. **User ID Hashing**: Ensure consistent hashing for percentage rollouts
4. **Audit Overhead**: Log asynchronously to avoid blocking evaluations

### Best Practices
1. **Environment Isolation**: Separate configs per environment
2. **Gradual Rollouts**: Start with low percentages
3. **Monitoring**: Track evaluation metrics and cache hit rates
4. **Testing**: Comprehensive testing of evaluation logic
5. **Documentation**: Keep flag purposes well-documented

## 🛠️ Setup & Running

### Prerequisites
- Java 17
- PostgreSQL
- Redis

### Configuration
Update `application.properties` with your database and Redis settings.

### Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

### API Documentation
Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

## 🎁 Future Enhancements

- Event-driven cache invalidation
- Kill-switch functionality
- Advanced targeting rules (segments, rules engine)
- Real-time dashboard with WebSockets
- Integration with CI/CD pipelines
- Multi-tenancy support

## 🤝 Contributing

This is a learning implementation. Key areas for improvement:
- Complete security implementation
- Comprehensive testing
- Performance benchmarking
- Frontend admin dashboard
- Advanced evaluation rules
