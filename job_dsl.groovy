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

job ("Tools/SEED") {
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

    steps {
        dsl {
            text('''
                def githubName = binding.variables.GITHUB_NAME
                def displayName = binding.variables.DISPLAY_NAME

                job(displayName) {
                    properties {
                        githubProjectUrl("https://github.com/${githubName}")
                    }

                    triggers {
                        scm('* * * * *')
                    }

                    scm {
                        git {
                            remote {
                                github(githubName)
                                credentials('github-credentials')
                            }
                            branches('*/main', '*/master')
                        }
                    }

                    wrappers {
                        preBuildCleanup()
                    }

                    steps {
                        shell('make fclean')
                        shell('make')
                        shell('make tests_run')
                        shell('make clean')
                    }
                }
            ''')
        }
    }
}