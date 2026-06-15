# 🔧 Chi Tiết Hướng Dẫn Setup

---

## **Table of Contents**

- [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
- [Windows Setup](#windows-setup)
- [Linux Setup](#linux-setup)
- [macOS Setup](#macos-setup)
- [Cấu Hình IDE (IntelliJ IDEA)](#cấu-hình-ide)
- [Xử Lý Sự Cố](#xử-lý-sự-cố)

---

## **Yêu Cầu Hệ Thống**

### **Bắt Buộc**
- ✅ **Java 17+** - [Download here](https://www.oracle.com/java/technologies/downloads/#java17)
- ✅ **Maven 3.6+** - [Download here](https://maven.apache.org/download.cgi)
- ✅ **MySQL 8.0+** - [Download here](https://dev.mysql.com/downloads/mysql/)
- ✅ **Git** - [Download here](https://git-scm.com/download)

### **Optional (Recommended)**
- ⚠️ **Redis 6.0+** - [Download here](https://redis.io/download)
- ⚠️ **IDE**: IntelliJ IDEA / VS Code
- ⚠️ **Postman** - Để test API

---

## **Windows Setup**

### **Step 1: Kiểm Tra Java & Maven**

```bash
# Check Java version
java -version

# Check Maven version
mvn -version
```

**Expected Output:**
```
java version "17.0.x" ...
Apache Maven 3.8.x ...
```

### **Step 2: Cài Đặt MySQL**

1. **Download MySQL Installer** từ [mysql.com](https://dev.mysql.com/downloads/mysql/)
2. **Run installer** và chọn "MySQL Server 8.0"
3. **Port**: 3306 (default)
4. **Username**: root
5. **Password**: Set secure password

### **Step 3: Verify MySQL**

```bash
# Connect to MySQL
mysql -u root -p

# Should show:
mysql> 
```

### **Step 4: Clone Project**

```bash
cd C:\Users\YourUsername\Projects
git clone https://github.com/yourrepo/Library-BE.git
cd Library-BE
```

### **Step 5: Create Database**

```bash
# Open MySQL command line
mysql -u root -p

# Execute:
CREATE DATABASE library CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library;
SOURCE src/main/resources/impact_db/DDL/database_22_05.sql;
SHOW TABLES;
EXIT;
```

### **Step 6: Configure Environment**

**Create file: `C:\Users\YourUsername\Projects\Library-BE\.env`**

```properties
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_very_long_and_secure_secret_key_min_32_characters_long
```

### **Step 7: Build & Run**

```bash
# Open CMD in project directory
cd C:\Users\YourUsername\Projects\Library-BE

# Build
mvn clean install

# Run
mvn spring-boot:run
```

**Expected Output:**
```
2026-06-15 10:30:00 - Started LibraryApplication in 5.234 seconds
```

### **Step 8: Test**

```bash
# Health check
curl http://localhost:8080/actuator/health

# Swagger UI
http://localhost:8080/swagger-ui.html
```

---

## **Linux Setup**

### **Step 1: Update System**

```bash
sudo apt update
sudo apt upgrade -y
```

### **Step 2: Install Java 17**

```bash
sudo apt install openjdk-17-jdk -y

# Verify
java -version
```

### **Step 3: Install Maven**

```bash
sudo apt install maven -y

# Verify
mvn -version
```

### **Step 4: Install MySQL 8.0**

```bash
sudo apt install mysql-server -y

# Secure installation
sudo mysql_secure_installation

# Start service
sudo systemctl start mysql
sudo systemctl enable mysql
```

### **Step 5: Create Database**

```bash
# Login to MySQL
sudo mysql -u root

# Run:
CREATE DATABASE library CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library;
SOURCE /path/to/database_22_05.sql;
CREATE USER 'library_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON library.* TO 'library_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### **Step 6: Clone & Setup Project**

```bash
# Create workspace
mkdir ~/projects
cd ~/projects

# Clone
git clone https://github.com/yourrepo/Library-BE.git
cd Library-BE

# Create .env file
cp .env.example .env

# Edit .env
nano .env
# Update: DB_USERNAME, DB_PASSWORD, JWT_SECRET
```

### **Step 7: Build & Run**

```bash
# Build
mvn clean install -DskipTests

# Run in background
nohup mvn spring-boot:run > library.log 2>&1 &

# Or foreground
mvn spring-boot:run
```

### **Step 8: Check Logs**

```bash
tail -f library.log
```

### **Step 9: Verify**

```bash
curl http://localhost:8080/actuator/health
```

---

## **macOS Setup**

### **Step 1: Install Homebrew (if not installed)**

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

### **Step 2: Install Java 17**

```bash
brew install openjdk@17

# Link to PATH
sudo ln -sfn /usr/local/opt/openjdk@17/libexec/openjdk.jdk \
     /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# Verify
java -version
```

### **Step 3: Install Maven**

```bash
brew install maven

# Verify
mvn -version
```

### **Step 4: Install MySQL**

```bash
# Option 1: Homebrew
brew install mysql@8.0

# Start service
brew services start mysql@8.0

# Option 2: Docker (easier)
docker run --name mysql8 -p 3306:3306 \
    -e MYSQL_ROOT_PASSWORD=your_password \
    -d mysql:8.0
```

### **Step 5: Create Database**

```bash
# Login
mysql -u root -p

# Run (same as Linux):
CREATE DATABASE library CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library;
SOURCE /path/to/database_22_05.sql;
EXIT;
```

### **Step 6: Clone & Setup**

```bash
mkdir ~/projects
cd ~/projects
git clone https://github.com/yourrepo/Library-BE.git
cd Library-BE

# Setup environment
cp .env.example .env
nano .env  # or: vim .env
```

### **Step 7: Build & Run**

```bash
mvn clean install
mvn spring-boot:run
```

### **Step 8: Verify**

```bash
curl http://localhost:8080/actuator/health
open http://localhost:8080/swagger-ui.html
```

---

## **Cấu Hình IDE**

### **IntelliJ IDEA**

#### **1. Import Project**

1. Mở IntelliJ IDEA
2. File → Open → Chọn folder `Library-BE`
3. Chọn "Open as Project"
4. Chờ indexing hoàn tất

#### **2. Configure JDK**

1. File → Project Structure → Project
2. SDK: Chọn JDK 17
3. Language level: 17
4. Click "Apply"

#### **3. Configure Maven**

1. File → Settings → Build, Execution, Deployment → Maven
2. Maven home path: Chọn Maven installation
3. Click "Apply"

#### **4. Enable Lombok**

1. File → Settings → Plugins
2. Search: "Lombok"
3. Install "Lombok" plugin
4. Restart IDE

#### **5. Run Application**

1. Run → Edit Configurations
2. Click "+" → Spring Boot
3. Name: "LibraryApplication"
4. Main class: `com.example.library.LibraryApplication`
5. Click "Run"

### **VS Code**

#### **1. Install Extensions**

- Extension Pack for Java
- Spring Boot Extension Pack
- MySQL

#### **2. Open Project**

```bash
code Library-BE
```

#### **3. Configure Launch**

File: `.vscode/launch.json`

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Spring Boot App",
            "request": "launch",
            "cwd": "${workspaceFolder}",
            "mainClass": "com.example.library.LibraryApplication",
            "projectName": "library",
            "preLaunchTask": "maven: clean",
            "console": "integratedTerminal"
        }
    ]
}
```

---

## **Xử Lý Sự Cố**

### **❌ Problem: `java: no such file or directory`**

**Solution:**
```bash
# Check Java PATH
which java

# If empty, add to .bashrc or .zshrc
export JAVA_HOME=/path/to/java17
export PATH=$JAVA_HOME/bin:$PATH

# Reload
source ~/.bashrc
```

### **❌ Problem: `mvn: command not found`**

**Solution:**
```bash
# Check Maven PATH
which mvn

# If empty, add to PATH
export MAVEN_HOME=/path/to/maven
export PATH=$MAVEN_HOME/bin:$PATH
```

### **❌ Problem: `Connection refused` (MySQL)**

```bash
# Windows
net start MySQL80

# Linux
sudo systemctl start mysql

# macOS
brew services start mysql@8.0
```

### **❌ Problem: Port 8080 Already in Use**

**Solution 1: Change Port**
```properties
# application.properties
server.port=9090
```

**Solution 2: Kill Process (Linux/Mac)**
```bash
lsof -i :8080
kill -9 <PID>
```

**Solution 2: Kill Process (Windows)**
```bash
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### **❌ Problem: JWT Secret Too Short**

**Error Message:**
```
JWT secret must be at least 32 characters
```

**Solution:**
```bash
# Generate strong secret
openssl rand -base64 32

# Or use online generator
# https://www.random.org/strings/

# Add to .env
JWT_SECRET=your_generated_secret_here
```

### **❌ Problem: Database Connection Error**

**Error:**
```
Access denied for user 'root'@'localhost'
```

**Solution:**
```bash
# Verify MySQL credentials
mysql -u root -p

# If password wrong, reset:
# https://dev.mysql.com/doc/mysql-installation-excerpt/8.0/en/resetting-permissions.html
```

### **❌ Problem: Maven Build Failure**

```bash
# Clean & rebuild
mvn clean install -DskipTests

# Check dependency
mvn dependency:tree

# Update dependencies
mvn dependency:update-sources
```

### **❌ Problem: Application Starts but APIs Return 401**

**Solution:**
- Enable Security in `LibraryApplication.java`
- Configure `SecurityConfig.java`
- Set proper JWT_SECRET in `.env`

---

## **Cấu Hình MySQL Profiles**

### **Development** (Reset data on restart)

```properties
# application-dev.properties
spring.datasource.url=jdbc:mysql://localhost:3306/library_dev
spring.datasource.username=root
spring.datasource.password=dev_password
spring.jpa.hibernate.ddl-auto=create-drop
logging.level.root=INFO
logging.level.com.example.library=DEBUG
```

### **Production** (Keep existing data)

```properties
# application-prod.properties
spring.datasource.url=jdbc:mysql://prod-server:3306/library
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
logging.level.root=WARN
logging.level.com.example.library=INFO
```

---

## **Performance Tuning**

### **MySQL**

```ini
# /etc/mysql/my.cnf or my.ini
[mysqld]
max_connections=1000
innodb_buffer_pool_size=1G
slow_query_log=ON
slow_query_log_file=/var/log/mysql/slow-query.log
long_query_time=2
```

### **Application**

```properties
# application.properties

# Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000

# Caching
spring.cache.type=redis
spring.redis.timeout=2000

# Logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.springframework.web=INFO
```

---

## **Testing Setup**

### **Unit Test**

```bash
mvn test
```

### **Integration Test**

```bash
mvn verify
```

### **Test Coverage**

```bash
mvn clean test jacoco:report
# Report at: target/site/jacoco/index.html
```

---

## **Docker Setup (Optional)**

### **Docker Compose**

File: `docker-compose.yml`

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root_pass
      MYSQL_DATABASE: library
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./src/main/resources/impact_db/DDL:/docker-entrypoint-initdb.d

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/library
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root_pass
    depends_on:
      - mysql
      - redis

volumes:
  mysql_data:
```

### **Run with Docker**

```bash
docker-compose up -d
docker-compose logs -f app
```

---

## **Cheat Sheet**

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Test
mvn test

# Package
mvn package -DskipTests

# Check dependency
mvn dependency:tree

# Database reset
mysql -u root -p library < src/main/resources/impact_db/DDL/database_22_05.sql

# View logs
tail -f nohup.out

# Kill process
kill -9 <PID>

# Hot reload
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.devtools.restart.enabled=true"
```

---

**Cần giúp? Tạo issue tại GitHub hoặc liên hệ admin@library.local** 🚀
