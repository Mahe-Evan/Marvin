folder("Tools") {
    displayName("Tools")
    description("Folder for miscellaneous tools.")
}

job("Tools/clone-repository") {
    parameters {
        stringParam {
            name("GIT_REPOSITORY_URL")
            description("Git URL of the repository to clone")
        }
    }

    wrappers {
        preBuildCleanup {
            includePattern('.git')
            includePattern('.gitmodules')
            includePattern('target/')
            includePattern('dist/')
            includePattern('build/')
            includePattern('out/')
            includePattern('node_modules/')
            includePattern('venv/')
            includePattern('__pycache__/')
            deleteDirectories()
            cleanupParameter('CLEANUP')
        }
    }

    steps {
        shell('git clone $GIT_REPOSITORY_URL')
    }

    publishers {
        wsCleanup()
    }
}

// Create a pipeline job that uses the pipeline definition
pipelineJob("EpitestPipeline") {
    parameters {
        stringParam {
            name("GITHUB_NAME")
            description("GitHub repository owner/repo_name (e.g.: \"EpitechIT31000/chocolatine\")")
        }
        stringParam {
            name("DISPLAY_NAME")
            description("Display name for the job")
        }
    }
    
    properties {
        githubProjectProperty {
            projectUrlStr('${env.GITHUB_USERNAME}')
        }
    }
    
    triggers {
        scm('* * * * *')
    }
    
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        github('${env.GITHUB_NAME}')
                        credentials('github-credentials')
                    }
                    branches('*/main', '*/master')
                }
            }
            scriptPath('Jenkinsfile')
        }
    }
}