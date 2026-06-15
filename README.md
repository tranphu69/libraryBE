# 📚 Library Management System - Backend

Hệ thống quản lý thư viện toàn diện được xây dựng bằng **Spring Boot 3.5.14**, cung cấp các API RESTful để quản lý người dùng, vai trò (roles), quyền hạn (permissions), và các chức năng cốt lõi của thư viện.

---

## 🎯 **Tính Năng Chính**

- ✅ **Quản lý Người Dùng**: Tạo, cập nhật, xóa, tìm kiếm người dùng với phân trang
- ✅ **Quản lý Vai Trò**: Gán quyền hạn cho vai trò
- ✅ **Quản lý Quyền Hạn**: Kiểm soát chi tiết các quyền truy cập
- ✅ **Import/Export Excel**: Nhập hàng loạt dữ liệu từ file Excel, xuất dữ liệu
- ✅ **Xác thực & Bảo mật**: JWT Token, BCrypt Password Encoding
- ✅ **Caching**: Redis integration (tối ưu hiệu năng)
- ✅ **Validation**: Input validation tự động
- ✅ **Exception Handling**: Xử lý lỗi tập trung, thông báo lỗi rõ ràng
- ✅ **API Documentation**: Swagger UI (OpenAPI 3.0)

---

## 📋 **Yêu Cầu Hệ Thống**

| Requirement | Version | Status |
|-------------|---------|--------|
| **Java** | 17+ | ✅ Required |
| **Maven** | 3.6+ | ✅ Required |
| **MySQL** | 5.7+ hoặc 8.0+ | ✅ Required |
| **Redis** | 6.0+ | ⚠️ Optional (cho caching) |
| **Spring Boot** | 3.5.14 | ✅ Configured |

---

## 🚀 **Quick Start - Hướng Dẫn Nhanh**

### **1. Clone Repository**
```bash
git clone <repository-url>
cd Library-BE
```

### **2. Cấu hình Database**
```bash
# Tạo database
mysql -u root -p
CREATE DATABASE library CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Import DDL
mysql -u root -p library < src/main/resources/impact_db/DDL/database_22_05.sql
```

### **3. Cấu hình Environment Variables**

Tạo file `.env` hoặc set environment variables:

```bash
# Database
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT Secret (tối thiểu 32 ký tự)
JWT_SECRET=your_secret_key_min_32_characters_long

# Redis (Optional)
REDIS_HOST=localhost
REDIS_PORT=6379
```

### **4. Cấu hình Application Properties**

**File: `application.properties`**
```properties
spring.application.name=library
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/library
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=validate

# Redis
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}

# JWT
jwt.secret=${JWT_SECRET:}
jwt.access-token-expiry=900000
jwt.refresh-token-expiry=604800000

# Logging
logging.level.root=INFO
logging.level.com.example.library=DEBUG
```

### **5. Build & Run**

```bash
# Build project
mvn clean install

# Run application
mvn spring-boot:run

# Hoặc dùng JAR
java -jar target/library-0.0.1-SNAPSHOT.jar
```

### **6. Kiểm Tra Application**

```bash
# Health check
curl http://localhost:8080/actuator/health

# Swagger UI
http://localhost:8080/swagger-ui.html

# API Docs
http://localhost:8080/v3/api-docs
```

---

## 📁 **Cấu Trúc Thư Mục**

```
Library-BE/
├── src/
│   ├── main/
│   │   ├── java/com/example/library/
│   │   │   ├── LibraryApplication.java          # Entry point
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java          # Spring Security config
│   │   │   ├── controller/
│   │   │   │   ├── UserController.java          # User APIs
│   │   │   │   ├── RoleController.java          # Role APIs
│   │   │   │   └── PermissionController.java    # Permission APIs
│   │   │   ├── service/
│   │   │   │   ├── UserService.java             # Business logic interface
│   │   │   │   ├── RoleService.java
│   │   │   │   ├── PermissionService.java
│   │   │   │   └── serviceImpl/                  # Implementation
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java          # Data access layer
│   │   │   │   ├── RoleRepository.java
│   │   │   │   ├── PermissionRepository.java
│   │   │   │   └── repositoryImpl/
│   │   │   ├── domain/                          # Entity classes
│   │   │   │   ├── User.java
│   │   │   │   ├── Role.java
│   │   │   │   └── Permission.java
│   │   │   ├── dto/
│   │   │   │   ├── request/                     # Request DTOs
│   │   │   │   └── response/                    # Response DTOs
│   │   │   ├── exception/
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── ErrorCode.java               # Error codes enum
│   │   │   │   └── GlobalExceptionHandler.java  # Global exception handler
│   │   │   ├── mapper/                          # MapStruct mappers
│   │   │   ├── constant/                        # Constants
│   │   │   └── util/
│   │   │       ├── DataUtils.java               # Data validation utilities
│   │   │       ├── ExcelUtils.java              # Excel utilities
│   │   │       └── ResponseUtils.java           # Response helpers
│   │   └── resources/
│   │       ├── application.properties           # Main config
│   │       ├── application-dev.properties       # Dev config
│   │       ├── application-prod.properties      # Prod config
│   │       ├── impact_db/
│   │       │   ├── DDL/
│   │       │   │   └── database_22_05.sql       # Database schema
│   │       │   └── DML/
│   │       └── template/                        # Excel templates
│   └── test/
│       └── java/com/example/library/
│           ├── service/                         # Service tests
│           ├── repository/                      # Repository tests
│           ├── controller/                      # Controller tests
│           └── LibraryApplicationTests.java
├── pom.xml                                       # Maven dependencies
├── mvnw                                          # Maven wrapper (Linux/Mac)
├── mvnw.cmd                                      # Maven wrapper (Windows)
└── README.md                                     # This file
```

---

## 🔌 **API Endpoints**

### **Base URL**: `http://localhost:8080/api`

### **User Management** (`/users`)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/users` | Tạo người dùng mới | ✅ Admin |
| PUT | `/users` | Cập nhật thông tin người dùng | ✅ Admin |
| DELETE | `/users/{id}` | Xóa người dùng (soft delete) | ✅ Admin |
| POST | `/users/search` | Tìm kiếm & lọc người dùng | ✅ User |
| GET | `/users/download-template` | Tải template Excel | ✅ User |
| POST | `/users/export` | Xuất dữ liệu người dùng | ✅ Admin |
| POST | `/users/import` | Nhập dữ liệu từ Excel | ✅ Admin |

**Create User Example**:
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "code": "USER001",
    "fullName": "John Doe",
    "password": "SecurePass@123",
    "listRole": [1, 2]
  }'
```

### **Role Management** (`/roles`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/roles` | Tạo vai trò |
| PUT | `/roles` | Cập nhật vai trò |
| DELETE | `/roles/{id}` | Xóa vai trò |
| POST | `/roles/search` | Tìm kiếm vai trò |
| GET | `/roles/all-status-active` | Lấy tất cả vai trò active |
| POST | `/roles/export` | Xuất vai trò |
| POST | `/roles/import` | Nhập vai trò |

### **Permission Management** (`/permissions`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/permissions` | Tạo quyền hạn |
| PUT | `/permissions` | Cập nhật quyền hạn |
| DELETE | `/permissions/{id}` | Xóa quyền hạn |
| POST | `/permissions/search` | Tìm kiếm quyền hạn |
| GET | `/permissions/all-status-active` | Lấy tất cả quyền active |
| POST | `/permissions/export` | Xuất quyền hạn |
| POST | `/permissions/import` | Nhập quyền hạn |

---

## 📊 **Response Format**

### **Success Response**
```json
{
  "code": "200",
  "success": true,
  "message": "Thành công",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "code": "USER001",
    "fullName": "John Doe",
    "email": "john@example.com",
    "roles": [
      {
        "id": 1,
        "code": "ADMIN",
        "name": "Administrator"
      }
    ]
  },
  "timestamp": "2026-06-15T10:30:00"
}
```

### **Error Response**
```json
{
  "code": "E01",
  "success": false,
  "message": "Mã người dùng không được để trống",
  "data": null,
  "timestamp": "2026-06-15T10:30:00"
}
```

### **Pagination Response**
```json
{
  "code": "200",
  "success": true,
  "message": "Thành công",
  "data": {
    "content": [...],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 50,
      "totalPages": 5,
      "first": true,
      "last": false
    }
  },
  "timestamp": "2026-06-15T10:30:00"
}
```

---

## 🔐 **Authentication & Authorization**

### **JWT Token Flow**

1. **Login**: Gửi `username` + `password`
2. **Get Token**: Nhận `access_token` + `refresh_token`
3. **Use Token**: Thêm vào header: `Authorization: Bearer <access_token>`
4. **Refresh**: Dùng `refresh_token` để lấy token mới khi hết hạn

### **Secure Endpoints**

```bash
curl -H "Authorization: Bearer eyJhbGc..." \
     http://localhost:8080/api/users/search
```

### **Role-based Access Control (RBAC)**

```
ADMIN   - Toàn quyền
USER    - Quyền hạn giới hạn
GUEST   - Chỉ xem
```

---

## 🗄️ **Database Schema**

### **Main Tables**

```sql
-- Users
CREATE TABLE USERS (
  ID VARCHAR(36) PRIMARY KEY,
  CODE VARCHAR(50) UNIQUE NOT NULL,
  EMAIL VARCHAR(100),
  FULL_NAME VARCHAR(200),
  PASSWORD VARCHAR(255) NOT NULL,
  IS_ACTIVE BOOLEAN,
  IS_DELETED BOOLEAN,
  IS_EMAIL_VERIFIED BOOLEAN,
  MFA_ENABLED BOOLEAN,
  IS_LOCKED BOOLEAN,
  CREATED_AT TIMESTAMP,
  UPDATED_AT TIMESTAMP
);

-- Roles
CREATE TABLE ROLES (
  ID BIGINT PRIMARY KEY AUTO_INCREMENT,
  CODE VARCHAR(50) UNIQUE NOT NULL,
  NAME VARCHAR(100),
  STATUS BIGINT,
  CREATED_AT TIMESTAMP,
  UPDATED_AT TIMESTAMP
);

-- Permissions
CREATE TABLE PERMISSIONS (
  ID BIGINT PRIMARY KEY AUTO_INCREMENT,
  CODE VARCHAR(50) UNIQUE NOT NULL,
  NAME VARCHAR(100),
  STATUS BIGINT,
  CREATED_AT TIMESTAMP,
  UPDATED_AT TIMESTAMP
);

-- Junction Tables
CREATE TABLE USERS_ROLES (
  USER_ID VARCHAR(36),
  ROLE_ID BIGINT,
  PRIMARY KEY (USER_ID, ROLE_ID),
  FOREIGN KEY (USER_ID) REFERENCES USERS(ID),
  FOREIGN KEY (ROLE_ID) REFERENCES ROLES(ID)
);

CREATE TABLE ROLES_PERMISSIONS (
  ROLE_ID BIGINT,
  PERMISSION_ID BIGINT,
  PRIMARY KEY (ROLE_ID, PERMISSION_ID),
  FOREIGN KEY (ROLE_ID) REFERENCES ROLES(ID),
  FOREIGN KEY (PERMISSION_ID) REFERENCES PERMISSIONS(ID)
);
```

---

## 🧪 **Testing**

### **Run Unit Tests**
```bash
mvn test
```

### **Run Integration Tests**
```bash
mvn verify
```

### **Code Coverage**
```bash
mvn clean test jacoco:report
# Report: target/site/jacoco/index.html
```

### **Example Test**
```bash
mvn test -Dtest=UserServiceTest#testCreateUser_Success
```

---

## 📦 **Dependencies**

### **Core**
- Spring Boot 3.5.14
- Spring Data JPA
- Spring Security
- MySQL Connector
- Lombok

### **API & Documentation**
- Springdoc OpenAPI (Swagger UI)

### **Authentication**
- JJWT (JWT Library)

### **Caching**
- Spring Data Redis

### **Excel Processing**
- Apache POI

### **Email**
- Spring Mail

### **Validation**
- Jakarta Validation

---

## ⚙️ **Configuration Profiles**

### **Development** (`-Dspring.profiles.active=dev`)
```properties
# Dev config - verbose logging, auto-update DB
spring.jpa.hibernate.ddl-auto=update
logging.level.com.example.library=DEBUG
```

### **Production** (`-Dspring.profiles.active=prod`)
```properties
# Prod config - validate only, minimal logging
spring.jpa.hibernate.ddl-auto=validate
logging.level.com.example.library=INFO
```

### **Run with Profile**
```bash
# Development
mvn spring-boot:run -Dspring.profiles.active=dev

# Production
mvn spring-boot:run -Dspring.profiles.active=prod
```

---

## 🐛 **Troubleshooting**

### **Connection Refused to MySQL**
```bash
# Check MySQL running
mysql -u root -p

# If not running:
# Windows: net start MySQL80
# Linux: sudo systemctl start mysql
```

### **JWT Secret Too Short**
```bash
Error: JWT secret must be at least 32 characters
Solution: Set JWT_SECRET environment variable with 32+ chars
```

### **Redis Connection Error**
```bash
# Redis không bắt buộc - ứng dụng vẫn chạy nếu Redis không khả dụng
# Nhưng caching sẽ không hoạt động
```

### **Port Already in Use**
```bash
# Change port in application.properties
server.port=9090
```

### **Excel Import Failed**
```bash
# Kiểm tra:
1. File format là .xlsx (Excel 2007+)
2. Columns match template
3. File size < 5MB
```

---

## 📝 **Error Codes**

| Code | Message | Solution |
|------|---------|----------|
| E01 | Không được để trống | Nhập giá trị |
| E02 | Không vượt quá X ký tự | Giảm độ dài |
| E03 | Giá trị không hợp lệ | Kiểm tra format |
| E04 | Không được trùng nhau | Chọn giá trị khác |
| E05 | Không tồn tại | Kiểm tra ID |
| E06 | Chỉ chứa chữ, số, dấu _ | Loại bỏ ký tự đặc biệt |
| E07 | Tệp không được để trống | Chọn file |
| E08 | Vượt quá X MB | Giảm kích thước |
| E09 | Định dạng sai | Dùng .xlsx |
| E10 | Tệp đang lỗi | Upload lại |

---

## 📚 **Documentation Links**

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [JWT Guide](https://jwt.io)
- [Swagger/OpenAPI](https://swagger.io)
- [MySQL Docs](https://dev.mysql.com/doc/)

---

## 🤝 **Contributing**

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

---

## 📄 **License**

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 📧 **Contact & Support**

- **Email**: admin@library.local
- **Issues**: [GitHub Issues](https://github.com/yourrepo/issues)
- **Documentation**: See `docs/` folder

---

## 🎉 **Version History**

### **v0.0.1** (Current - 2026-06-15)
- ✅ User Management
- ✅ Role Management
- ✅ Permission Management
- ✅ Excel Import/Export
- ✅ JWT Authentication (In Progress)
- ✅ API Documentation

### **Roadmap**
- [ ] Advanced Search Filters
- [ ] Email Verification
- [ ] Two-Factor Authentication (2FA)
- [ ] Audit Logging
- [ ] Performance Optimization
- [ ] Mobile App Integration

---

**Happy Coding! 🚀**

Last updated: June 15, 2026
