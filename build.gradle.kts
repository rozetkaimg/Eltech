subprojects {
    tasks.matching { it.name.contains("AarMetadata") }.configureEach {
        enabled = false
    }
}
