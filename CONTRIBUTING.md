# 🤝 Contributing Guide

Cảm ơn bạn quan tâm đến project này! Tài liệu này hướng dẫn quy trình đóng góp.

---

## **Table of Contents**

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)

---

## **Code of Conduct**

### **Mục đích chúng ta**

- 🤗 Respectful và inclusive community
- 📝 Clear & professional communication
- 🚀 Focus on quality & performance
- 🐛 Constructive feedback

### **Những gì KHÔNG làm**

- ❌ Harassment hoặc discrimination
- ❌ Spam comments hoặc PRs
- ❌ Pushing incomplete code to main
- ❌ Ignoring security issues

---

## **Getting Started**

### **1. Fork Repository**

```bash
# Go to: https://github.com/yourrepo/Library-BE
# Click "Fork" button
```

### **2. Clone Your Fork**

```bash
git clone https://github.com/YOUR_USERNAME/Library-BE.git
cd Library-BE

# Add upstream remote
git remote add upstream https://github.com/ORIGINAL_OWNER/Library-BE.git
```

### **3. Create Feature Branch**

```bash
# Sync with upstream
git fetch upstream
git checkout -b feature/your-feature main

# Or bugfix branch
git checkout -b bugfix/issue-description main
```

### **4. Setup Development Environment**

```bash
# See SETUP.md
mvn clean install
mvn spring-boot:run
```

---

## **Development Workflow**

### **Before You Code**

1. **Check existing issues** - Avoid duplicate work
2. **Create/assign issue** - Link to your PR later
3. **Discuss major changes** - Use issue comments for discussion

### **While You Code**

```
project/
├── src/main/java/com/example/library/
│   ├── controller/          # HTTP endpoints
│   ├── service/             # Business logic
│   │   └── serviceImpl/
│   ├── repository/          # Data access
│   ├── domain/              # Entity classes
│   ├── dto/                 # Data transfer objects
│   ├── exception/           # Custom exceptions
│   ├── config/              # Spring configs
│   ├── mapper/              # MapStruct mappers
│   ├── constant/            # Constants
│   └── util/                # Utility classes
└── src/test/java/           # Test files
```

### **File Naming**

- **Controllers**: `*Controller.java` → `UserController.java`
- **Services**: `*Service.java` + `*ServiceImpl.java`
- **Repositories**: `*Repository.java`
- **Entities**: `*.java` → `User.java`
- **DTOs**: `*Request.java`, `*Response.java`
- **Tests**: `*Test.java` → `UserServiceTest.java`

### **After You Code**

```bash
# Test your changes
mvn test

# Check code quality
mvn checkstyle:check
mvn pmd:check

# Build
mvn clean install

# Format code
mvn spotless:apply
```

---

## **Commit Guidelines**

### **Commit Message Format**

```
<type>(<scope>): <subject>

<body>

<footer>
```

### **Types**

| Type | Use Case |
|------|----------|
| **feat** | New feature |
| **fix** | Bug fix |
| **docs** | Documentation |
| **style** | Code style (formatting, etc) |
| **refactor** | Code refactoring |
| **perf** | Performance improvement |
| **test** | Test additions/updates |
| **chore** | Dependencies, build tools |

### **Scope**

- **user**: User management
- **role**: Role management
- **permission**: Permission management
- **auth**: Authentication
- **api**: API endpoints
- **db**: Database related
- **config**: Configuration
- **util**: Utilities
- **test**: Testing

### **Examples**

```bash
git commit -m "feat(user): add user search with pagination"

git commit -m "fix(auth): resolve JWT token expiration bug"

git commit -m "docs(readme): update setup instructions"

git commit -m "refactor(service): extract validation logic to utility"

git commit -m "test(user): add unit tests for UserService"
```

### **Body Content**

```
feat(user): add user search with pagination

- Implement search API endpoint with filters
- Add pagination support (page, size, sort)
- Add input validation for search parameters
- Add unit tests with 85% coverage

Closes #123
Related to #456
```

### **Footer**

- **Closes #123** - Closes the issue
- **Related to #456** - Links to related issue
- **Breaking Change**: `yes` if API changes

---

## **Pull Request Process**

### **1. Before Submitting**

```bash
# Sync with upstream
git fetch upstream
git rebase upstream/main

# Ensure tests pass
mvn test

# Check code style
mvn spotless:check
```

### **2. Push to Your Fork**

```bash
git push origin feature/your-feature
```

### **3. Create Pull Request**

- **Title**: Use commit message format → `feat(user): add search with pagination`
- **Description**: Use template below
- **Link Issue**: `Closes #123`
- **Labels**: Select appropriate labels

### **PR Template**

```markdown
## 📝 Description

Brief description of changes and why

## 🎯 Type of Change

- [ ] 🚀 New feature
- [ ] 🐛 Bug fix
- [ ] 📚 Documentation
- [ ] ♻️ Refactoring
- [ ] 🎨 Style
- [ ] 🧪 Test

## 📋 Checklist

- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments for complex logic added
- [ ] Documentation updated
- [ ] Tests added/updated (coverage > 80%)
- [ ] All tests passing
- [ ] No breaking changes

## 🔗 Related Issues

Closes #123

## 📸 Screenshots (if applicable)

## 🧪 Testing

Describe how to test changes:
1. Step 1
2. Step 2
3. Verify result

## ✨ Additional Notes

Any additional context
```

### **PR Review Process**

1. **Automated Checks**
   - ✅ Tests pass
   - ✅ Code coverage maintained
   - ✅ Style guidelines followed

2. **Code Review**
   - 👀 Maintainers review code
   - 💬 Discussion & feedback
   - 🔄 Request changes if needed

3. **Approval & Merge**
   - ✅ At least 1 approval
   - ✅ All feedback addressed
   - ✅ Ready to merge

---

## **Coding Standards**

### **Java Conventions**

```java
// ✅ Good
public class UserService {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String USER_NOT_FOUND = "User not found";
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserResponse findById(String id) {
        return userRepository.findById(id)
            .map(userMapper::toResponse)
            .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
    }
}

// ❌ Bad
public class user_service {  // lowercase
    private int page_size = 20;  // no final, no static
    
    public user_response find_user_by_id(string id) {  // snake_case
        if (userRepository.findById(id) != null) {
            return userRepository.findById(id);  // duplicate call
        }
        return null;  // no exception
    }
}
```

### **Naming Conventions**

| Type | Format | Example |
|------|--------|---------|
| Class | PascalCase | `UserService` |
| Method | camelCase | `getUserById()` |
| Variable | camelCase | `userId` |
| Constant | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE` |
| Package | lowercase | `com.example.library` |
| Interface | PascalCase | `UserRepository` |

### **Comments & Documentation**

```java
/**
 * Retrieves a user by their unique identifier.
 *
 * @param id the user ID
 * @return the user response DTO
 * @throws NotFoundException if user not found
 * @since 1.0.0
 */
public UserResponse getUserById(String id) {
    // Validate input
    if (DataUtils.isBlank(id)) {
        throw new InvalidArgumentException("User ID cannot be blank");
    }
    
    // Query database
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("User not found"));
    
    // Map to response
    return userMapper.toResponse(user);
}
```

### **Exception Handling**

```java
// ✅ Good
try {
    user = userRepository.save(user);
} catch (DataIntegrityViolationException e) {
    throw new BusinessException(ErrorCode.DUPLICATE_EMAIL, "Email already exists", e);
} catch (Exception e) {
    logger.error("Unexpected error saving user", e);
    throw new InternalServerException("Failed to save user", e);
}

// ❌ Bad
try {
    user = userRepository.save(user);
} catch (Exception e) {
    e.printStackTrace();  // Don't use this
}
```

### **Logging**

```java
// ✅ Good
logger.info("User created successfully: {}", user.getId());
logger.warn("High memory usage detected: {}%", memoryUsage);
logger.error("Failed to save user: {}", user.getCode(), exception);
logger.debug("Search criteria: code={}, email={}", code, email);

// ❌ Bad
System.out.println("User: " + user);  // Don't use System.out
logger.info("DEBUG: " + userObject.toString());  // Too verbose
logger.error(userObject);  // Missing context
```

---

## **Testing Guidelines**

### **Unit Tests**

```java
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    public void testGetUserById_Success() {
        // Arrange
        String userId = "123";
        User user = User.builder()
            .id(userId)
            .code("USER001")
            .fullName("John Doe")
            .build();
        
        when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));
        
        // Act
        UserResponse response = userService.getUserById(userId);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("USER001");
        verify(userRepository).findById(userId);
    }
    
    @Test
    public void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById("999"))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(NotFoundException.class, 
            () -> userService.getUserById("999"));
    }
}
```

### **Test Coverage**

- **Minimum**: 70%
- **Target**: 80%+
- **Critical paths**: 100%

```bash
# Check coverage
mvn clean test jacoco:report

# View report
open target/site/jacoco/index.html
```

### **Integration Tests**

```java
@SpringBootTest
@Transactional
public class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void testCreateUser() throws Exception {
        UserRequest request = UserRequest.builder()
            .code("USER001")
            .fullName("John Doe")
            .build();
        
        mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

---

## **Documentation**

### **Update README**

- 📝 Add new features
- 📝 Update API endpoints
- 📝 Update setup instructions

### **Update CHANGELOG**

Format (Semantic Versioning):

```markdown
## [1.1.0] - 2026-06-20

### Added
- User search with pagination (#123)
- Excel import/export for roles (#124)

### Fixed
- JWT token expiration bug (#125)
- Database connection pool leak (#126)

### Changed
- Refactored validation logic (#127)

### Deprecated
- Old search API endpoint (will be removed in 2.0.0)
```

### **Add Swagger Docs**

```java
@PostMapping
@Operation(summary = "Create user", 
           description = "Create a new user with roles")
@ApiResponse(responseCode = "200", description = "User created")
@ApiResponse(responseCode = "400", description = "Invalid input")
public ResponseEntity<ApiResponse<UserResponse>> create(
    @Valid @RequestBody UserRequest request) {
    return ResponseEntity.ok(userService.create(request));
}
```

---

## **Common Issues & Solutions**

### **Build Fails**

```bash
# Clean & rebuild
mvn clean install -DskipTests

# Update dependencies
mvn dependency:update-sources
```

### **Tests Fail After Pull**

```bash
# Sync database
mysql -u root -p library < src/main/resources/impact_db/DDL/database_22_05.sql

# Rebuild
mvn clean test
```

### **Code Style Issues**

```bash
# Format code
mvn spotless:apply

# Check
mvn spotless:check
```

---

## **Getting Help**

- 📖 **Documentation**: Check README.md & SETUP.md
- 💬 **Discussion**: Open GitHub discussion
- 🐛 **Issue**: Report bugs with detailed info
- 📧 **Contact**: admin@library.local

---

## **Recognition**

We appreciate all contributions! Contributors will be recognized in:
- 📝 CONTRIBUTORS.md
- 🏆 Commit history
- 🌟 GitHub profile

---

**Happy Contributing! 🚀**

Let's build something amazing together!
