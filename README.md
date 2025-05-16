# My Marvin

## Table of Contents

- [Overview](#overview)
- [Goals](#goals)
- [Features](#features)
- [Compilation / Usage](#compilation--usage)
- [Contributing](#contributing)
- [License](#license)

## Overview

**MyMarvin** demonstrates how to fully configure and automate a Jenkins instance using **Configuration as Code (JCasC)** and **Job DSL**. It sets up global settings, user accounts, roles, folders, and jobs purely through code, enabling reproducible, version-controlled CI/CD infrastructure.

## Goals

* Define Jenkins global settings and security through a single YAML file (`my_marvin.yml`).
* Automate creation of Jenkins jobs, folders, and pipeline logic using a centralized DSL script (`job_dsl.groovy`).
* Ensure the configuration is testable and can be validated via automated tests (Marvin evaluating your Marvin!).
* Provide a foundation for DevOps best practices by codifying Jenkins setup and job workflows.

## Features

* **JCasC Configuration**: All core Jenkins settings, system message, security, users, and roles are declared in `my_marvin.yml`.
* **Role-Based Authorization**: Defines four roles (`admin`, `ape`, `gorilla`, `assist`) with precisely scoped permissions assigned to specific users.
* **Predefined Users**: Creates Hugo, Garance, Jeremy, and Nassim with passwords injected via environment variables.
* **Tools Folder**: A root folder named `Tools` to group utility jobs.
* **Utility Jobs**:

  * `clone-repository`: Freestyle job for manual repository cloning with parameterized Git URL and workspace cleanup.
  * `SEED`: Freestyle job that uses Job DSL to generate pipeline jobs based on GitHub repo inputs.
* **Seed-Generated Jobs**: Jobs created by `SEED` are configured for GitHub SCM polling, cleanup, and standard Make-based build/test steps (`make fclean`, `make`, `make tests_run`, `make clean`).

## Usage

1. **Prepare environment variables**

   * Copy the example env file:

     ```bash
     cp .env.example .env
     ```
   * Edit `.env` to set the following variables:

     ```ini
     USER_CHOCOLATEEN_PASSWORD=...
     USER_VAUGIE_G_PASSWORD=...
     USER_I_DONT_KNOW_PASSWORD=...
     USER_NASSO_PASSWORD=...
     GITHUB_USERNAME=...
     GITHUB_TOKEN=...
     ```
2. **Launch with Docker Compose**

   * Build and start the Jenkins container using the provided `docker-compose.yml` and `Dockerfile`:

     ```bash
     docker-compose up --build -d
     ```
   * This sets up:

     * Jenkins LTS (with JCasC and Job DSL plugins)
     * Mounts `my_marvin.yml`, `job_dsl.groovy`, and `.env` into the container
3. **Access Jenkins**

   * Open your browser at `http://localhost:8080` and verify:

     * System message is set.
     * Users and roles exist with appropriate permissions.
     * `Tools` folder and jobs (`clone-repository`, `SEED`) are present.
4. **Generate pipeline jobs**

   * Run the `SEED` job manually to create project-specific jobs based on your GitHub repository inputs.

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository and create your feature branch (`git checkout -b feature/XYZ`).
2. Commit your changes (`git commit -am 'Add new feature'`).
3. Push to the branch (`git push origin feature/XYZ`).
4. Open a Pull Request describing your changes.

Ensure all modifications to `my_marvin.yml` or `job_dsl.groovy` remain fully testable and do not introduce hardcoded credentials.

## Contributors

- Evan MAHE : [GitHub/Mahe-Evan](https://github.com/Mahe-Evan)
- Enoal FAUCHILLE-BOLLE : [GitHub/Enoal-Fauchille-Bolle](https://github.com/Enoal-Fauchille-Bolle)

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
