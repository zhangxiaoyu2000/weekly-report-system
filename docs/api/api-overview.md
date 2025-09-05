# API Documentation

This document provides a comprehensive overview of the Weekly Report System REST API.

## Base URL

- **Development**: `http://localhost:8080/api`
- **Test**: `https://api-test.weekly-report.example.com`
- **Production**: `https://api.weekly-report.example.com`

## Authentication

The API uses JWT (JSON Web Token) for authentication. Include the token in the Authorization header:

```http
Authorization: Bearer <your-jwt-token>
```

### Authentication Endpoints

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "token": "jwt-token-string",
  "user": {
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "EMPLOYEE"
  },
  "expiresIn": 3600
}
```

#### Refresh Token
```http
POST /auth/refresh
Authorization: Bearer <current-token>
```

#### Logout
```http
POST /auth/logout
Authorization: Bearer <token>
```

## API Endpoints

### User Management

#### Get Current User
```http
GET /users/me
Authorization: Bearer <token>
```

#### Update Current User
```http
PUT /users/me
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "string",
  "lastName": "string",
  "email": "string"
}
```

#### Change Password
```http
POST /users/me/password
Authorization: Bearer <token>
Content-Type: application/json

{
  "currentPassword": "string",
  "newPassword": "string"
}
```

#### Get All Users (Admin/Manager only)
```http
GET /users?page=0&size=20&sort=username,asc
Authorization: Bearer <token>
```

#### Create User (Admin only)
```http
POST /users
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "role": "EMPLOYEE|MANAGER|ADMIN",
  "password": "string"
}
```

#### Update User (Admin only)
```http
PUT /users/{userId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "role": "EMPLOYEE|MANAGER|ADMIN",
  "active": true
}
```

#### Delete User (Admin only)
```http
DELETE /users/{userId}
Authorization: Bearer <token>
```

### Report Management

#### Get My Reports
```http
GET /reports/my?page=0&size=20&sort=weekStartDate,desc
Authorization: Bearer <token>
```

Query Parameters:
- `page` (integer): Page number (0-based)
- `size` (integer): Number of items per page
- `sort` (string): Sort criteria (field,direction)
- `weekStartDate` (date): Filter by week start date
- `status` (string): Filter by status (DRAFT, SUBMITTED, APPROVED)

#### Get Report by ID
```http
GET /reports/{reportId}
Authorization: Bearer <token>
```

#### Create Report
```http
POST /reports
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "string",
  "weekStartDate": "2023-12-04",
  "content": "string",
  "accomplishments": [
    {
      "description": "string",
      "category": "DEVELOPMENT|MEETINGS|RESEARCH|OTHER"
    }
  ],
  "challenges": [
    {
      "description": "string",
      "impact": "HIGH|MEDIUM|LOW"
    }
  ],
  "nextWeekPlans": [
    {
      "description": "string",
      "priority": "HIGH|MEDIUM|LOW"
    }
  ]
}
```

#### Update Report
```http
PUT /reports/{reportId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "string",
  "content": "string",
  // ... same structure as create
}
```

#### Submit Report
```http
POST /reports/{reportId}/submit
Authorization: Bearer <token>
```

#### Delete Report
```http
DELETE /reports/{reportId}
Authorization: Bearer <token>
```

#### Get Team Reports (Manager/Admin only)
```http
GET /reports?page=0&size=20&userId={userId}&weekStartDate={date}
Authorization: Bearer <token>
```

#### Approve Report (Manager/Admin only)
```http
POST /reports/{reportId}/approve
Authorization: Bearer <token>
Content-Type: application/json

{
  "feedback": "string (optional)"
}
```

#### Reject Report (Manager/Admin only)
```http
POST /reports/{reportId}/reject
Authorization: Bearer <token>
Content-Type: application/json

{
  "reason": "string (required)"
}
```

### Dashboard & Analytics

#### Get Dashboard Data
```http
GET /dashboard
Authorization: Bearer <token>
```

**Response:**
```json
{
  "totalReports": 42,
  "submittedThisWeek": 8,
  "pendingApproval": 3,
  "averageSubmissionTime": "2.3 days",
  "recentReports": [...],
  "submissionTrends": [
    {
      "week": "2023-12-04",
      "submitted": 15,
      "total": 20
    }
  ]
}
```

#### Get Team Analytics (Manager/Admin only)
```http
GET /analytics/team?startDate={date}&endDate={date}
Authorization: Bearer <token>
```

#### Get User Performance (Manager/Admin only)
```http
GET /analytics/users/{userId}/performance?period=MONTH|QUARTER|YEAR
Authorization: Bearer <token>
```

### File Management

#### Upload Attachment
```http
POST /files/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <binary-data>
```

**Response:**
```json
{
  "id": "uuid",
  "filename": "document.pdf",
  "size": 1024000,
  "contentType": "application/pdf",
  "uploadDate": "2023-12-04T10:30:00Z",
  "url": "/files/uuid/download"
}
```

#### Download File
```http
GET /files/{fileId}/download
Authorization: Bearer <token>
```

#### Delete File
```http
DELETE /files/{fileId}
Authorization: Bearer <token>
```

### Export & Reporting

#### Export Reports to PDF
```http
GET /exports/reports/pdf?startDate={date}&endDate={date}&userId={id}
Authorization: Bearer <token>
Accept: application/pdf
```

#### Export Reports to Excel
```http
GET /exports/reports/excel?startDate={date}&endDate={date}&userId={id}
Authorization: Bearer <token>
Accept: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

#### Generate Team Summary
```http
GET /exports/team-summary?weekStartDate={date}
Authorization: Bearer <token>
Accept: application/pdf
```

### Notifications

#### Get My Notifications
```http
GET /notifications?page=0&size=20&unreadOnly=true
Authorization: Bearer <token>
```

#### Mark Notification as Read
```http
PUT /notifications/{notificationId}/read
Authorization: Bearer <token>
```

#### Get Notification Preferences
```http
GET /notifications/preferences
Authorization: Bearer <token>
```

#### Update Notification Preferences
```http
PUT /notifications/preferences
Authorization: Bearer <token>
Content-Type: application/json

{
  "emailNotifications": true,
  "reportReminders": true,
  "approvalNotifications": true,
  "deadlineAlerts": true
}
```

## Data Models

### User
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "role": "ADMIN|MANAGER|EMPLOYEE",
  "active": true,
  "createdAt": "2023-12-04T10:30:00Z",
  "updatedAt": "2023-12-04T10:30:00Z"
}
```

### Report
```json
{
  "id": 1,
  "title": "string",
  "weekStartDate": "2023-12-04",
  "content": "string",
  "status": "DRAFT|SUBMITTED|APPROVED|REJECTED",
  "submittedAt": "2023-12-04T10:30:00Z",
  "approvedAt": "2023-12-04T10:30:00Z",
  "feedback": "string",
  "user": {
    // User object
  },
  "accomplishments": [
    {
      "id": 1,
      "description": "string",
      "category": "DEVELOPMENT|MEETINGS|RESEARCH|OTHER"
    }
  ],
  "challenges": [
    {
      "id": 1,
      "description": "string",
      "impact": "HIGH|MEDIUM|LOW"
    }
  ],
  "nextWeekPlans": [
    {
      "id": 1,
      "description": "string",
      "priority": "HIGH|MEDIUM|LOW"
    }
  ],
  "attachments": [
    {
      // File objects
    }
  ],
  "createdAt": "2023-12-04T10:30:00Z",
  "updatedAt": "2023-12-04T10:30:00Z"
}
```

### Notification
```json
{
  "id": 1,
  "title": "string",
  "message": "string",
  "type": "REMINDER|APPROVAL|REJECTION|DEADLINE",
  "read": false,
  "createdAt": "2023-12-04T10:30:00Z",
  "relatedReportId": 123
}
```

## Error Handling

### Standard Error Response
```json
{
  "timestamp": "2023-12-04T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/reports",
  "details": [
    {
      "field": "title",
      "message": "Title is required"
    }
  ]
}
```

### HTTP Status Codes

- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `204 No Content`: Request successful, no content to return
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Authentication required or invalid
- `403 Forbidden`: Access denied
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict (e.g., duplicate username)
- `422 Unprocessable Entity`: Validation errors
- `500 Internal Server Error`: Server error

## Rate Limiting

The API implements rate limiting to ensure fair usage:

- **Authenticated requests**: 1000 requests per hour per user
- **Authentication requests**: 10 requests per minute per IP
- **File uploads**: 20 requests per minute per user

Rate limit headers are included in responses:
```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1701691800
```

## Pagination

List endpoints support pagination using the following query parameters:

- `page`: Page number (0-based, default: 0)
- `size`: Page size (default: 20, max: 100)
- `sort`: Sort criteria in format `field,direction` (e.g., `createdAt,desc`)

Paginated responses include metadata:
```json
{
  "content": [...],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

## Swagger/OpenAPI

Interactive API documentation is available at:
- **Development**: http://localhost:8080/swagger-ui.html
- **Test**: https://api-test.weekly-report.example.com/swagger-ui.html

The OpenAPI specification can be accessed at:
- `/v3/api-docs` (JSON format)
- `/v3/api-docs.yaml` (YAML format)

## SDK and Libraries

### JavaScript/TypeScript
```javascript
// Example using fetch API
const token = localStorage.getItem('jwt-token');

const response = await fetch('/api/reports/my', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

const reports = await response.json();
```

### cURL Examples
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Get reports
curl -X GET http://localhost:8080/api/reports/my \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Webhooks (Future Feature)

Planned webhook support for external integrations:

- Report submission notifications
- Approval/rejection events
- User creation/updates
- Deadline reminders

## Changelog

### v1.0.0
- Initial API implementation
- User management endpoints
- Report CRUD operations
- Authentication with JWT
- File upload support
- Dashboard analytics

### v1.1.0 (Planned)
- Webhook support
- Advanced filtering options
- Bulk operations
- API versioning

---

For additional API support, please contact the development team or create an issue in the GitHub repository.