FROM jenkins/jenkins:lts

USER root

# Remove the setup wizard
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false

# Install Configuration as Code plugin
RUN jenkins-plugin-cli --plugins configuration-as-code:latest

# Install Job DSL plugin
RUN jenkins-plugin-cli --plugins job-dsl:latest

# Install additional plugins
RUN jenkins-plugin-cli --plugins \
    cloudbees-folder:latest \
    credentials:latest \
    github:latest \
    instance-identity:latest \
    script-security:latest \
    structs:latest \
    role-strategy:latest \
    ws-cleanup:latest \
    workflow-job:latest \
    envinject:latest

# Install Docker
RUN apt-get update && \
    apt-get install -y apt-transport-https ca-certificates curl gnupg2 software-properties-common && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add - && \
    echo "deb [arch=amd64] https://download.docker.com/linux/debian buster stable" > /etc/apt/sources.list.d/docker.list && \
    apt-get update && \
    apt-get install -y docker-ce-cli

# Add Jenkins user to Docker group
RUN groupadd docker && usermod -aG docker jenkins