# Starfleet Gamifier

## Overview
Starfleet Gamifier is a Spring Boot application designed to gamify activities within the Starfleet organization. It utilizes MongoDB for data storage and provides a structured way to track user achievements and ranks.

## Project Structure
```
gamifier
├── src
│   └── main
│       └── resources
│           └── application.yml
├── docker-compose.yml
├── .env
└── README.md
```

## Setup Instructions

### Prerequisites
- Docker and Docker Compose installed on your machine.
- Java Development Kit (JDK) 11 or higher.

### Configuration
1. **MongoDB Configuration**: The application connects to a MongoDB instance running on `localhost:27017`. Ensure that MongoDB is running before starting the application.

2. **Environment Variables**: You can define environment variables in the `.env` file to customize your setup.

### Running the Application
1. Clone the repository:
   ```
   git clone <repository-url>
   cd starfleet-gamifier
   ```

2. Start the MongoDB service using Docker Compose:
   ```
   docker-compose up -d
   ```

3. Run the Spring Boot application using your globally installed Maven:
   ```
    
   ```

### Accessing the Application
Once the application is running, you can access it at `http://localhost:8080`.

### Logging
The application logs are configured to show debug-level information for the `com.starfleet` and `org.springframework.data.mongodb` packages. Check the console for detailed logs.

### Contribution
Feel free to contribute to the project by submitting issues or pull requests. 

### License
This project is licensed under the MIT License. See the LICENSE file for more details.