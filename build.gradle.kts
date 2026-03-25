subprojects {
    apply(plugin = "jacoco")

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        finalizedBy("jacocoTestReport")
    }

    tasks.withType<JacocoReport>().configureEach {
        reports {
            xml.required.set(true)
        }
    }
}
