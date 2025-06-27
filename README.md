# Tech Support Server

A comprehensive Spring Boot REST API application for managing technical support operations, including client management, ticket tracking, technician assignment, appointment scheduling, and feedback collection.

## 🚀 Features

- **Client Management**: Register and manage client information and support levels
- **Ticket System**: Create, assign, track, and resolve support tickets
- **Technician Management**: Manage technician profiles, skills, and availability
- **Appointment Scheduling**: Schedule and manage service appointments
- **Feedback System**: Collect and analyze customer feedback
- **Real-time Statistics**: Get insights into support operations
- **MySQL Database**: Production-ready database with comprehensive sample data
- **Sample Data**: Pre-loaded realistic test data for development and testing

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.6+**
- **MySQL 8.0+**

## 🛠️ Quick Start

### Database Setup
```bash
# 1. Install and start MySQL
brew install mysql
brew services start mysql

# 2. Create database and user
mysql -u root -p
```

```sql
CREATE DATABASE techsupport;
CREATE USER 'techsupport'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON techsupport.* TO 'techsupport'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### Run the Application
```bash
# Clone the repository
git clone <your-repo-url>
cd local-tech-support-server

# Run application with MySQL and sample data
mvn spring-boot:run -Dspring-boot.run.profiles=mysql-dev
```

The application will automatically:
- Create all necessary database tables
- Load comprehensive sample data
- Start the server on `http://localhost:8080`

## 🌐 API Endpoints

Base URL: `http://localhost:8080/api`

### Clients
- `GET /clients` - Get all clients (with pagination, sorting, filtering)
- `GET /clients/{id}` - Get client by ID
- `POST /clients` - Create new client
- `PUT /clients/{id}` - Update client
- `DELETE /clients/{id}` - Delete client
- `GET /clients/search?query={term}` - Search clients
- `GET /clients/statistics` - Get client statistics

### Tickets
- `GET /tickets` - Get all tickets (with filters)
- `GET /tickets/{id}` - Get ticket by ID
- `POST /tickets` - Create new ticket
- `PUT /tickets/{id}` - Update ticket
- `POST /tickets/{id}/assign` - Assign ticket to technician
- `POST /tickets/{id}/close` - Close ticket
- `GET /tickets/client/{clientId}` - Get tickets by client
- `GET /tickets/technician/{technicianId}` - Get tickets by technician

### Technicians
- `GET /technicians` - Get all technicians
- `GET /technicians/{id}` - Get technician by ID
- `POST /technicians` - Create new technician
- `PUT /technicians/{id}` - Update technician
- `GET /technicians/available` - Get available technicians
- `GET /technicians/{id}/workload` - Get technician workload

### Appointments
- `GET /appointments` - Get all appointments
- `GET /appointments/{id}` - Get appointment by ID
- `POST /appointments` - Create new appointment
- `PUT /appointments/{id}` - Update appointment
- `POST /appointments/{id}/complete` - Complete appointment
- `POST /appointments/{id}/cancel` - Cancel appointment

### Feedback
- `GET /feedback` - Get all feedback
- `POST /feedback` - Submit feedback
- `GET /feedback/ticket/{ticketId}` - Get feedback by ticket
- `GET /feedback/statistics` - Get feedback statistics

## 📊 Sample Data

The application comes pre-loaded with comprehensive sample data:

- **8 Clients** with diverse profiles and contact information
- **5 Technicians** with different specializations and statuses
- **15+ Tickets** in various states (open, closed, overdue)
- **Multiple Appointments** scheduled across different time periods
- **Feedback Entries** with ratings and comments
- **Ticket History** tracking all status changes

## 🔧 Environment Variables

You can customize database connection using environment variables:

```bash
export DB_USERNAME=your_mysql_username
export DB_PASSWORD=your_mysql_password
export DATABASE_URL=jdbc:mysql://localhost:3306/your_database

mvn spring-boot:run -Dspring-boot.run.profiles=mysql-dev
```

## 🧪 Testing the API

### Using curl
```bash
# Get all clients
curl -X GET http://localhost:8080/api/clients

# Create a new client
curl -X POST http://localhost:8080/api/clients \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "phone": "555-1234",
    "address": "123 Test St"
  }'

# Get all tickets
curl -X GET http://localhost:8080/api/tickets
```

### Using Browser
- View clients: `http://localhost:8080/api/clients`
- View tickets: `http://localhost:8080/api/tickets`
- View technicians: `http://localhost:8080/api/technicians`

## 🗄️ Database Access

Connect to your MySQL database to explore the data:

```bash
mysql -u techsupport -p techsupport
# Password: password
```

Common queries:
```sql
-- View all tables
SHOW TABLES;

-- Check data counts
SELECT COUNT(*) FROM clients;
SELECT COUNT(*) FROM technicians;
SELECT COUNT(*) FROM tickets;

-- View sample data
SELECT id, first_name, last_name, email FROM clients LIMIT 5;
SELECT id, title, status FROM tickets WHERE status = 'OPEN';
```

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/localtechsupport/
│   │   ├── controller/          # REST API controllers
│   │   ├── service/            # Business logic services
│   │   ├── repository/         # Data access repositories
│   │   ├── entity/            # JPA entities
│   │   ├── dto/               # Data transfer objects
│   │   └── exception/         # Exception handling
│   └── resources/
│       ├── application.yml    # Main configuration
│       ├── application-dev.yml # Development profile
│       ├── data.sql          # Sample data
│       └── data-mysql.sql    # MySQL-specific sample data
└── test/                     # Unit and integration tests
```

## 🐛 Troubleshooting

### MySQL Connection Issues
```bash
# Check if MySQL is running
brew services list | grep mysql

# Restart MySQL
brew services restart mysql

# Check MySQL logs
tail -f /opt/homebrew/var/mysql/$(hostname).err
```

### Application Won't Start
```bash
# Check Java version
java -version

# Clean and rebuild
mvn clean compile

# Check for port conflicts
lsof -i :8080
```

### Database Issues
```bash
# Verify database exists
mysql -u techsupport -p -e "SHOW DATABASES;"

# Check table creation
mysql -u techsupport -p techsupport -e "SHOW TABLES;"

# Verify sample data loaded
mysql -u techsupport -p techsupport -e "SELECT COUNT(*) as client_count FROM clients;"
```

## 🔒 Security Notes

**Development Configuration**: The current setup uses default credentials for development. For production deployment:

1. Change default passwords
2. Use environment variables for sensitive data
3. Enable HTTPS
4. Add authentication and authorization
5. Configure proper CORS settings

## 📝 API Documentation

After starting the application, you can explore the API using:
- **Browser**: Direct endpoint access for GET requests
- **Postman**: Import the provided collection for comprehensive testing
- **curl**: Command-line testing examples provided above

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Commit changes: `git commit -am 'Add feature'`
4. Push to branch: `git push origin feature-name`
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
