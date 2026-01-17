# Calendar Application

A full-stack calendar application built with Spring Boot (backend) and React (frontend) that allows users to manage events, set reminders, create recurring events, and organize their schedule with a modern, responsive interface.

## 🚀 Features

- **User Management**: User registration, authentication, and profile management
- **Calendar View**: Interactive calendar interface with monthly, weekly, and daily views
- **Event Management**: Create, edit, delete, and view events
- **Recurring Events**: Support for repeating events (daily, weekly, monthly, yearly)
- **Reminders**: Set notifications for upcoming events
- **Event Tags**: Categorize and organize events with custom tags
- **Data Persistence**: Reliable data storage with H2 database
- **Security**: JWT-based authentication and authorization
- **Responsive Design**: Modern UI built with React and Tailwind CSS

## 📋 Prerequisites

Before running this application, ensure you have the following installed on your machine:

### Required Software

1. **Java Development Kit (JDK) 17 or higher**
    - Download from: https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/
    - Verify installation: `java -version`

2. **Node.js 18 or higher**
    - Download from: https://nodejs.org/
    - Verify installation: `node --version` and `npm --version`

3. **Git**
    - Download from: https://git-scm.com/
    - Verify installation: `git --version`

### Optional but Recommended

- **IDE**: IntelliJ IDEA, VS Code, or Eclipse
- **Postman**: For API testing (https://www.postman.com/)

## 🛠️ Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd ss25-ai-lab-final-project-lorencrrapaj
```

### 2. Backend Setup

Navigate to the backend directory:

```bash
cd backend
```

#### Build and Run the Backend

The backend uses Gradle wrapper, so you don't need to install Gradle separately.

**On Linux/macOS:**
```bash
./gradlew bootRun
```

**On Windows:**
```bash
gradlew.bat bootRun
```

The backend will start on `http://localhost:8082`

#### Alternative: Build and Run JAR

```bash
# Build the application
./gradlew build

# Run the JAR file
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

### 3. Frontend Setup

Open a new terminal and navigate to the frontend directory:

```bash
cd frontend
```

#### Install Dependencies

```bash
npm install
```

#### Start the Development Server

```bash
npm run dev
```

The frontend will start on `http://localhost:5173`

## 🏃‍♂️ Running the Application

1. **Start the Backend**: Follow the backend setup instructions above
2. **Start the Frontend**: Follow the frontend setup instructions above
3. **Access the Application**: Open your browser and go to `http://localhost:5173`

### Database Access

The application uses H2 in-memory database. You can access the H2 console at:
- URL: `http://localhost:8082/h2-console`
- JDBC URL: `jdbc:h2:file:./data/calendar;DB_CLOSE_ON_EXIT=FALSE`
- Username: `sa`
- Password: (leave empty)

## 🧪 Running Tests

### Backend Tests

Navigate to the backend directory and run:

```bash
cd backend

# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew jacocoTestReport

# Run code quality checks (Checkstyle, PMD, SpotBugs)
./gradlew check

# View test results
# Test reports: build/reports/tests/test/index.html
# Coverage report: build/reports/jacoco/test/html/index.html
# Checkstyle report: build/reports/checkstyle/main.html
# PMD report: build/reports/pmd/main.html
# SpotBugs report: build/reports/spotbugs/main.html
```

### Frontend Tests

Navigate to the frontend directory and run:

```bash
cd frontend

# Run unit tests
npm test

# Run tests in watch mode
npm run test:watch

# Run tests with coverage
npm run test:coverage

# Run end-to-end tests
npm run e2e

# Open Cypress test runner
npm run e2e:open

# View test results
# Coverage report: coverage/lcov-report/index.html
```

### Code Quality and Formatting

#### Backend
```bash
cd backend

# Run Checkstyle
./gradlew checkstyleMain checkstyleTest

# Run PMD
./gradlew pmdMain pmdTest

# Run SpotBugs
./gradlew spotbugsMain spotbugsTest
```

#### Frontend
```bash
cd frontend

# Run ESLint
npm run lint

# Format code with Prettier
npm run format

# Check code formatting
npm run format:check
```

## 📁 Project Structure

```
├── backend/                 # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/        # Java source code
│   │   │   └── resources/   # Configuration files
│   │   └── test/            # Test files
│   ├── build.gradle         # Gradle build configuration
│   └── gradlew             # Gradle wrapper
├── frontend/               # React frontend
│   ├── src/
│   │   ├── components/     # React components
│   │   ├── pages/          # Page components
│   │   ├── styles/         # CSS styles
│   │   └── __tests__/      # Test files
│   ├── package.json        # Node.js dependencies
│   └── vite.config.ts      # Vite configuration
└── README.md              # This file
```

## 🔧 Configuration

### Backend Configuration

- **Port**: 8082 (configurable in `application.yml`)
- **Database**: H2 file database stored in `./data/calendar`
- **Profiles**: Default profile includes H2 console access

### Frontend Configuration

- **Port**: 5173 (default Vite port)
- **API Base URL**: `http://localhost:8082`
- **Build Tool**: Vite
- **Testing**: Jest + React Testing Library

## 🐛 Troubleshooting

### Common Issues

1. **Port Already in Use**
    - Backend: Change port in `backend/src/main/resources/application.yml`
    - Frontend: Vite will automatically suggest an alternative port

2. **Java Version Issues**
    - Ensure you're using Java 17 or higher
    - Check with `java -version`

3. **Node.js Version Issues**
    - Ensure you're using Node.js 18 or higher
    - Check with `node --version`

4. **Database Issues**
    - Delete the `backend/data` directory to reset the database
    - Check H2 console for database state

5. **Build Failures**
    - Clean and rebuild: `./gradlew clean build` (backend) or `npm run build` (frontend)
    - Check for dependency conflicts

### Getting Help

- Check the console output for error messages
- Verify all prerequisites are installed correctly
- Ensure both backend and frontend are running simultaneously
- Check that no other applications are using the required ports

## 📊 Quality Metrics

The project includes comprehensive quality assurance:

- **Test Coverage**: Minimum 90% code coverage required
- **Code Style**: Checkstyle with custom rules
- **Bug Detection**: SpotBugs static analysis
- **Code Quality**: PMD rules enforcement
- **Frontend Testing**: Jest unit tests + Cypress E2E tests
- **Code Formatting**: Prettier for consistent formatting

## 🚀 Deployment

For production deployment:

1. **Backend**: Build with `./gradlew build` and deploy the JAR file
2. **Frontend**: Build with `npm run build` and serve the `dist` directory
3. **Database**: Configure a production database (PostgreSQL, MySQL, etc.)
4. **Environment**: Set appropriate environment variables for production

## 📝 API Documentation

The backend exposes RESTful APIs for:
- User authentication and management
- Event CRUD operations
- Calendar data retrieval
- Recurring event management

Access the API documentation at `http://localhost:8082/actuator` (health and info endpoints are exposed).

---

**Note**: This application was developed as part of the SS25 AI Lab Final Project. For any issues or questions, please refer to the project documentation or contact the development team.
