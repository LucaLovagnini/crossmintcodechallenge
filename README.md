# Crossmint Challenge

## Overview
The Crossmint Challenge is a Spring Boot command-line application designed to interact with the Crossmint API. The application allows users to create, delete, and manage astral objects on a goal map using a flexible and extensible command-line interface.

## Juicy Notes and Assumptions
* Application has been designed to be as simple and self-contained as possible, therefore Spring has been used for convenient tools (e.g., DI for SOLID code, WebClient and Reactor for easy and parallel API calls with retry mechanisms)
* Solution leverage the undocumented (😉) `/api/map/[candidateId]`, which retrieve the current status of the map. This is used for efficiently delete all elements in the current map in `deleteAll` and `replicateGoal` commands.
* Parallel calls, backoff, jitter and request delays are implemented (see `AstralObjectService`), but they have *not* been fine-tuned.   
* Solution assumes that `/api/map` endpoint(s) will always return the same amount of columns for each row (i.e., only the length of the first row is checked).
* Solution includes a **deprecated** version of the first phase command called `CreateXShapePolyanetCommand`. The author implemented it as a fast-forward solution, but the `replicateGoal` command covers the first phase solution as well. Deprecated class is left as reference.
* Performing a `DELETE /api/polyanets` can actually delete *any* astral object (this is being used in both delete commands described below). It is not ideal for sure (and in a real case scenario we should create the proper object), but we kept the deleting logic simple.

## Features
* Create various astral objects:
   * Polyanets
   * Soloons (with different colors)
   * Comeths (with different directions)
* Delete specific or all astral objects
* Replicate a predefined goal map
* Robust error handling and retry mechanisms
* Parallel processing of API requests

## Technology Stack
* Java 21
* Spring Boot 3.2.3
* PicoCLI for command-line interface
* WebClient for reactive HTTP requests
* JUnit 5 for testing
* Mockito for mocking
* Jacoco for code coverage

## Prerequisites
* Java Development Kit (JDK) 21
* Maven 3.6 or higher

## Project Structure

```
crossmint-challenge
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── crossmint
│   │   │           └── challenge
│   │   │               ├── commands           # Command implementations
│   │   │               ├── config             # Configuration classes 
│   │   │               ├── model              # Domain models
│   │   │               └── service            # Business logic services
│   │   └── resources           
│   │       └── application.properties
│   └── test
│       ├── java
│       │   └── com
│       │       └── crossmint
│       │           └── challenge
│       │               ├── commands
│       │               ├── model
│       │               └── service
│       └── resources
├── pom.xml
└── README.md
```

## Setup and Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/crossmint-challenge.git
   cd crossmint-challenge
   ```

2. Build the project:
   ```bash 
   mvn clean install
   ```

## Running the Application
Using Maven, you can run the application and pass command-line arguments in one step:

```bash
mvn clean package spring-boot:run -Dspring-boot.run.arguments="<command> <arguments>"
```

## Available Commands
1. Create Astral Objects:
   ```bash
   # Create a Polyanet
   mvn clean package spring-boot:run -Dspring-boot.run.arguments="create polyanet <x> <y>"
   
   # Create a Soloon with a specific color  
   mvn clean package spring-boot:run -Dspring-boot.run.arguments="create soloon <x> <y> <color>"

   # Create a Cometh with a specific direction
   mvn clean package spring-boot:run -Dspring-boot.run.arguments="create cometh <x> <y> <direction>"
   ```

2. Delete Objects:
   ```bash
   # Delete a specific astral object
   mvn clean package spring-boot:run -Dspring-boot.run.arguments="delete <x> <y>"

   # Delete all astral objects
   mvn clean package spring-boot:run -Dspring-boot.run.arguments="deleteall"
   ```

3. Replicate Goal Map:

   **IMPORTANT:** executing the `replicategoal` will first clear the whole map first.

   ```bash
   mvn clean package spring-boot:run -Dspring-boot.run.arguments="replicategoal"
   ```

## Configuration
Key configuration parameters are defined in `src/main/resources/application.properties`:
* `crossmint.candidate-id`: Your unique candidate identifier
* `crossmint.api.base-url`: Base URL for the Crossmint API
* `crossmint.parallel-degree`: Number of parallel API requests
* Retry and request delay configurations

## Testing
Run the test suite:
```bash
mvn test
```

Run tests with code coverage report:
```bash
mvn verify
```

The coverage report will be generated in `target/site/jacoco/index.html`

## Error Handling
The application includes robust error handling:
* Retry mechanism for rate limiting and server errors (5 by default)
* Detailed logging
* Coordinate validation
* Graceful error reporting

## License
Distributed under the MIT License. See `LICENSE` file for more information.