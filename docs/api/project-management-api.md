# Project Management API Documentation

This document provides comprehensive documentation for the Project Management API endpoints in the Weekly Report System.

## Base URL

- **Development**: `http://localhost:8080/api`
- **Test**: `https://api-test.weekly-report.example.com`
- **Production**: `https://api.weekly-report.example.com`

## Authentication

All Project Management API endpoints require JWT authentication. Include the token in the Authorization header:

```http
Authorization: Bearer <your-jwt-token>
```

## Project Management Endpoints

### Projects

#### List Projects
```http
GET /api/projects
Authorization: Bearer <token>
```

**Query Parameters:**
- `page` (integer, optional): Page number (0-based, default: 0)
- `size` (integer, optional): Page size (default: 20, max: 100)
- `sort` (string, optional): Sort criteria (e.g., `name,asc`, `createdAt,desc`)
- `search` (string, optional): Search term for project name/description
- `status` (string, optional): Filter by project status (`PLANNING`, `ACTIVE`, `ON_HOLD`, `COMPLETED`)
- `priority` (string, optional): Filter by priority (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`)
- `startDate` (date, optional): Filter by start date (ISO format: YYYY-MM-DD)
- `endDate` (date, optional): Filter by end date (ISO format: YYYY-MM-DD)
- `managerId` (integer, optional): Filter by project manager ID
- `includeMembers` (boolean, optional): Include member count in response

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Weekly Report System",
      "description": "Development of comprehensive weekly reporting platform",
      "status": "ACTIVE",
      "priority": "HIGH",
      "startDate": "2025-01-01",
      "endDate": "2025-12-31",
      "progress": 75,
      "budget": 50000.00,
      "actualCost": 37500.00,
      "manager": {
        "id": 2,
        "username": "project.manager",
        "fullName": "John Manager",
        "email": "john.manager@company.com"
      },
      "memberCount": 8,
      "createdAt": "2025-01-01T10:00:00Z",
      "updatedAt": "2025-09-05T15:30:00Z",
      "createdBy": {
        "id": 1,
        "username": "admin",
        "fullName": "System Admin"
      }
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 45,
    "totalPages": 3
  }
}
```

#### Get Project by ID
```http
GET /api/projects/{projectId}
Authorization: Bearer <token>
```

**Path Parameters:**
- `projectId` (integer, required): Project ID

**Query Parameters:**
- `includeMembers` (boolean, optional): Include full member list
- `includeReports` (boolean, optional): Include related weekly reports
- `includeStatistics` (boolean, optional): Include project statistics

**Response:**
```json
{
  "id": 1,
  "name": "Weekly Report System",
  "description": "Development of comprehensive weekly reporting platform",
  "status": "ACTIVE",
  "priority": "HIGH",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "progress": 75,
  "budget": 50000.00,
  "actualCost": 37500.00,
  "tags": ["development", "web", "java"],
  "manager": {
    "id": 2,
    "username": "project.manager",
    "fullName": "John Manager",
    "email": "john.manager@company.com",
    "role": "DEPARTMENT_MANAGER"
  },
  "members": [
    {
      "id": 10,
      "user": {
        "id": 3,
        "username": "developer1",
        "fullName": "Jane Developer",
        "email": "jane.dev@company.com"
      },
      "role": "MEMBER",
      "permissions": ["READ", "WRITE"],
      "joinedAt": "2025-01-05T09:00:00Z"
    }
  ],
  "statistics": {
    "totalMembers": 8,
    "activeMembers": 7,
    "completedTasks": 45,
    "totalTasks": 60,
    "averageReportScore": 8.5
  },
  "createdAt": "2025-01-01T10:00:00Z",
  "updatedAt": "2025-09-05T15:30:00Z",
  "createdBy": {
    "id": 1,
    "username": "admin",
    "fullName": "System Admin"
  }
}
```

#### Create Project
```http
POST /api/projects
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "New Project",
  "description": "Project description",
  "status": "PLANNING",
  "priority": "MEDIUM",
  "startDate": "2025-10-01",
  "endDate": "2025-12-31",
  "budget": 25000.00,
  "managerId": 2,
  "tags": ["development", "web"],
  "goals": [
    {
      "description": "Complete API development",
      "targetDate": "2025-11-15",
      "priority": "HIGH"
    }
  ]
}
```

**Response:**
```json
{
  "id": 15,
  "name": "New Project",
  "description": "Project description",
  "status": "PLANNING",
  "priority": "MEDIUM",
  "startDate": "2025-10-01",
  "endDate": "2025-12-31",
  "progress": 0,
  "budget": 25000.00,
  "actualCost": 0.00,
  "manager": {
    "id": 2,
    "username": "project.manager",
    "fullName": "John Manager"
  },
  "memberCount": 1,
  "createdAt": "2025-09-05T16:00:00Z",
  "updatedAt": "2025-09-05T16:00:00Z",
  "createdBy": {
    "id": 1,
    "username": "admin",
    "fullName": "System Admin"
  }
}
```

#### Update Project
```http
PUT /api/projects/{projectId}
Authorization: Bearer <token>
Content-Type: application/json
```

**Path Parameters:**
- `projectId` (integer, required): Project ID

**Request Body:**
```json
{
  "name": "Updated Project Name",
  "description": "Updated description",
  "status": "ACTIVE",
  "priority": "HIGH",
  "startDate": "2025-10-01",
  "endDate": "2025-12-31",
  "budget": 30000.00,
  "progress": 25,
  "tags": ["development", "web", "urgent"]
}
```

#### Delete Project
```http
DELETE /api/projects/{projectId}
Authorization: Bearer <token>
```

**Path Parameters:**
- `projectId` (integer, required): Project ID

**Response:** `204 No Content`

### Project Members

#### List Project Members
```http
GET /api/projects/{projectId}/members
Authorization: Bearer <token>
```

**Path Parameters:**
- `projectId` (integer, required): Project ID

**Query Parameters:**
- `role` (string, optional): Filter by member role (`PROJECT_MANAGER`, `MEMBER`, `OBSERVER`)
- `search` (string, optional): Search by username or full name
- `active` (boolean, optional): Filter active/inactive members
- `page` (integer, optional): Page number
- `size` (integer, optional): Page size

**Response:**
```json
[
  {
    "id": 10,
    "user": {
      "id": 3,
      "username": "developer1",
      "fullName": "Jane Developer",
      "email": "jane.dev@company.com",
      "role": "EMPLOYEE",
      "department": "Engineering",
      "position": "Senior Developer"
    },
    "projectRole": "MEMBER",
    "permissions": ["READ", "WRITE", "COMMENT"],
    "joinedAt": "2025-01-05T09:00:00Z",
    "lastActivity": "2025-09-05T14:30:00Z",
    "contributionScore": 8.5,
    "isActive": true
  }
]
```

#### Get Project Member by ID
```http
GET /api/projects/{projectId}/members/{memberId}
Authorization: Bearer <token>
```

#### Add Project Member
```http
POST /api/projects/{projectId}/members
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "userId": 5,
  "role": "MEMBER",
  "permissions": ["READ", "WRITE"],
  "notifyUser": true,
  "message": "Welcome to the project!"
}
```

**Response:**
```json
{
  "id": 25,
  "user": {
    "id": 5,
    "username": "newmember",
    "fullName": "New Member",
    "email": "new.member@company.com"
  },
  "projectRole": "MEMBER",
  "permissions": ["READ", "WRITE"],
  "joinedAt": "2025-09-05T16:15:00Z",
  "isActive": true
}
```

#### Update Project Member
```http
PUT /api/projects/{projectId}/members/{memberId}
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "role": "PROJECT_MANAGER",
  "permissions": ["READ", "WRITE", "MANAGE", "DELETE"],
  "notifyUser": true
}
```

#### Remove Project Member
```http
DELETE /api/projects/{projectId}/members/{memberId}
Authorization: Bearer <token>
```

**Query Parameters:**
- `transferTasks` (boolean, optional): Transfer member's tasks to manager
- `notifyUser` (boolean, optional): Send removal notification

### Project Statistics and Analytics

#### Get Project Statistics
```http
GET /api/projects/statistics
Authorization: Bearer <token>
```

**Response:**
```json
{
  "totalProjects": 45,
  "activeProjects": 12,
  "completedProjects": 28,
  "onHoldProjects": 3,
  "planningProjects": 2,
  "projectsByPriority": {
    "CRITICAL": 3,
    "HIGH": 8,
    "MEDIUM": 25,
    "LOW": 9
  },
  "averageProjectDuration": 180,
  "budgetUtilization": 78.5,
  "memberDistribution": {
    "PROJECT_MANAGER": 15,
    "MEMBER": 120,
    "OBSERVER": 25
  }
}
```

#### Get Project Dashboard
```http
GET /api/projects/dashboard
Authorization: Bearer <token>
```

**Response:**
```json
{
  "myProjects": {
    "managed": 3,
    "member": 7,
    "observer": 2
  },
  "recentActivity": [
    {
      "type": "PROJECT_CREATED",
      "projectId": 15,
      "projectName": "New Project",
      "timestamp": "2025-09-05T16:00:00Z",
      "actor": "System Admin"
    }
  ],
  "upcomingDeadlines": [
    {
      "projectId": 8,
      "projectName": "Q4 Report System",
      "deadline": "2025-12-31",
      "daysRemaining": 117,
      "priority": "HIGH"
    }
  ],
  "performanceMetrics": {
    "onTimeDelivery": 85.5,
    "budgetCompliance": 92.3,
    "teamSatisfaction": 8.7
  }
}
```

## Data Models

### Project
```json
{
  "id": "integer",
  "name": "string (required, max: 255)",
  "description": "string (max: 2000)",
  "status": "enum (PLANNING, ACTIVE, ON_HOLD, COMPLETED, CANCELLED)",
  "priority": "enum (LOW, MEDIUM, HIGH, CRITICAL)",
  "startDate": "date (ISO format)",
  "endDate": "date (ISO format)",
  "progress": "integer (0-100)",
  "budget": "decimal",
  "actualCost": "decimal",
  "tags": "array of strings",
  "manager": "User object",
  "members": "array of ProjectMember objects",
  "goals": "array of ProjectGoal objects",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "createdBy": "User object"
}
```

### ProjectMember
```json
{
  "id": "integer",
  "user": "User object",
  "projectRole": "enum (PROJECT_MANAGER, MEMBER, OBSERVER)",
  "permissions": "array of strings (READ, WRITE, MANAGE, DELETE, COMMENT)",
  "joinedAt": "datetime",
  "lastActivity": "datetime",
  "contributionScore": "decimal",
  "isActive": "boolean"
}
```

### ProjectGoal
```json
{
  "id": "integer",
  "description": "string",
  "targetDate": "date",
  "priority": "enum (LOW, MEDIUM, HIGH)",
  "status": "enum (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)",
  "assignedTo": "User object",
  "completedAt": "datetime"
}
```

## Error Handling

### Standard Error Response
```json
{
  "timestamp": "2025-09-05T16:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/projects",
  "details": [
    {
      "field": "name",
      "message": "Project name is required",
      "rejectedValue": null
    },
    {
      "field": "endDate",
      "message": "End date must be after start date",
      "rejectedValue": "2024-12-31"
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
- `403 Forbidden`: Access denied (insufficient permissions)
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict (e.g., duplicate project name in department)
- `422 Unprocessable Entity`: Validation errors
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: Server error

## Permission Requirements

### Project Permissions
- **View Projects**: All authenticated users can view projects they are members of
- **Create Projects**: ADMIN, DEPARTMENT_MANAGER, HR_MANAGER roles
- **Update Projects**: Project managers and admins
- **Delete Projects**: Project managers and admins (with confirmation)

### Member Management Permissions
- **View Members**: All project members
- **Add Members**: Project managers and admins
- **Update Member Roles**: Project managers and admins
- **Remove Members**: Project managers and admins
- **Leave Project**: Members can remove themselves

## Rate Limiting

- **Project Operations**: 100 requests per minute per user
- **Member Operations**: 50 requests per minute per user
- **Statistics**: 20 requests per minute per user

## Validation Rules

### Project Name
- Required field
- Length: 3-255 characters
- Must be unique within the department
- Cannot contain special characters: `<>\"'`

### Project Dates
- Start date cannot be in the past (for new projects)
- End date must be after start date
- Date format: ISO 8601 (YYYY-MM-DD)

### Budget
- Must be positive number
- Maximum value: 10,000,000
- Precision: 2 decimal places

### Member Roles
- `PROJECT_MANAGER`: Full project control
- `MEMBER`: Read/write access to project data
- `OBSERVER`: Read-only access

## Integration Examples

### JavaScript/Fetch API
```javascript
// Get projects with filtering
const response = await fetch('/api/projects?status=ACTIVE&priority=HIGH', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
const projects = await response.json();

// Create new project
const newProject = {
  name: 'Q1 Analytics Dashboard',
  description: 'Customer analytics dashboard for Q1',
  status: 'PLANNING',
  priority: 'HIGH',
  startDate: '2025-10-01',
  endDate: '2025-12-31',
  budget: 45000.00,
  managerId: 5
};

const createResponse = await fetch('/api/projects', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(newProject)
});
```

### cURL Examples
```bash
# List active projects
curl -X GET "http://localhost:8080/api/projects?status=ACTIVE" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Create project
curl -X POST "http://localhost:8080/api/projects" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Project",
    "description": "Project description",
    "status": "PLANNING",
    "priority": "MEDIUM",
    "startDate": "2025-10-01",
    "endDate": "2025-12-31",
    "budget": 25000.00,
    "managerId": 2
  }'

# Add project member
curl -X POST "http://localhost:8080/api/projects/1/members" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 5,
    "role": "MEMBER",
    "permissions": ["READ", "WRITE"]
  }'
```

## Changelog

### v1.0.0 (Planned)
- Initial project management API implementation
- Project CRUD operations
- Member management functionality
- Permission-based access control
- Statistics and analytics endpoints

---

For additional support or questions, please contact the development team or create an issue in the GitHub repository.