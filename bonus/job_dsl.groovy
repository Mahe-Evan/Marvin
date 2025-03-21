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

        stringParam {
            name("EXECUTABLE")
            description("Name of the executable to run")
        }
    }

    steps {
        dsl {
            text('''
                def githubName = binding.variables.GITHUB_NAME
                def displayName = binding.variables.DISPLAY_NAME
                def executable = binding.variables.EXECUTABLE

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

                    environmentVariables {
                        env('WORKSPACE', "/mnt/jenkins_builds/${'$'}{JOB_NAME}_${'$'}{BUILD_ID}")
                        env('EXECUTABLE', executable)
                    }

                    steps {
                        shell("""
                            # Set up the workspace
                            : "############################################################"
                            : "#                   Setting up Workspace                   #"
                            : "############################################################"
                            mkdir -p /mnt/jenkins_builds
                            mkdir -p ${'$'}WORKSPACE
                            cp -r * ${'$'}WORKSPACE
                        """.stripIndent())

                        shell("""
                            # Compile the project
                            : "###########################################################"
                            : "#                    Compiling Project                    #"
                            : "###########################################################"

                            cd ${'$'}WORKSPACE

                            # Run the Docker container
                            docker exec mymarvin-epitest-1 bash -c "cd ${'$'}WORKSPACE && (make clean) > output.txt"
                            cat output.txt
                            docker exec mymarvin-epitest-1 bash -c "cd ${'$'}WORKSPACE && (make) > output.txt"
                            cat output.txt

                            # Check if the executable exists
                            if [ ! -f "${'$'}WORKSPACE/${'$'}EXECUTABLE" ]; then
                                : "❌ Executable not found! Exiting with status 1."
                                exit 1
                            else
                                : "✅ Executable found: ${'$'}EXECUTABLE"
                            fi

                            # Clean up the workspace
                            docker exec mymarvin-epitest-1 bash -c "cd ${'$'}WORKSPACE && (make fclean) > output.txt"
                            cat output.txt
                        """.stripIndent())

                        shell("""
                            # Check the coding style
                            : "###########################################################"
                            : "#                  Checking Coding Style                  #"
                            : "###########################################################"

                            cd ${'$'}WORKSPACE

                            # Run the coding style checker
                            docker run --rm --security-opt "label:disable" -i \
                            -v "${'$'}WORKSPACE":"/mnt/delivery" \
                            -v "${'$'}WORKSPACE":"/mnt/reports" \
                            ghcr.io/epitech/coding-style-checker:latest "/mnt/delivery" "/mnt/reports"

                            # Check if the report file exists
                            if [ -s "coding-style-reports.log" ]; then
                                cat coding-style-reports.log
                                : "❌ Coding style errors detected! Exiting with status 1."
                                exit 1
                            else
                                : "✅ No coding style issues found."
                            fi

                        """.stripIndent())

                        shell("""
                            # Clean up the workspace
                            : "###########################################################"
                            : "#                  Cleaning up Workspace                  #"
                            : "###########################################################"
                            rm -rf ${'$'}WORKSPACE
                        """.stripIndent())
                    }
                }
            ''')
        }
    }
}
