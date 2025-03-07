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
