# ToyS3

ToyS3 is a distributed S3 system created for learning purposes. This project aims to help developers understand the concepts of distributed systems, object storage, and other related technologies.

## Purpose

The main goal of ToyS3 is to provide a hands-on experience with building a distributed S3 system. By working on this project, you will gain knowledge and skills in:

- Distributed systems
- Object storage
- Akka HTTP
- Scala
- Docker

## Installation

To run ToyS3, you need to have Docker and Docker Compose installed on your machine. Follow the instructions below to set up and run the project.

### Prerequisites

- Docker: [Install Docker](https://docs.docker.com/get-docker/)
- Docker Compose: [Install Docker Compose](https://docs.docker.com/compose/install/)

### Running the Project

1. Clone the repository:

    ```sh
    git clone https://github.com/your-username/toys3.git
    cd toys3
    ```

2. Build and start the services using Docker Compose:

    ```sh
    docker-compose up
    ```

This command will build the Docker image and start the Akka HTTP server. The application will be accessible at `http://localhost:8080`.

## Contributing

Contributions are welcome! Please fork the repository and create a pull request to contribute to the project.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
