folder("Tools") {
    displayName("Tools")
    description("Folder for miscellaneous tools.")
}

job("Tools/test") {
    steps {
        shell("echo 'Hello, World!'")
    }
}
