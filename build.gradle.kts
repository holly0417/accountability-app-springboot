import net.nemerosa.versioning.VersioningExtension
import org.gradle.kotlin.dsl.invoke
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.springframework.boot.gradle.dsl.SpringBootExtension
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("idea")
    id("java")
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("net.nemerosa.versioning") version "3.1.0"
    id("org.siouan.frontend-jdk21") version "10.0.0"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.data:spring-data-commons")
    implementation ("org.springframework.boot:spring-boot-starter-mail")
    implementation("com.github.ben-manes.caffeine:caffeine")


    implementation("org.slf4j:slf4j-api")
    implementation("com.h2database:h2")
    implementation("org.flywaydb:flyway-core")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.security:spring-security-test")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

configure<SpringBootExtension> {
    buildInfo()
}

configure<IdeaModel> {
    module {
        inheritOutputDirs = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_24.toString()
    targetCompatibility = JavaVersion.VERSION_24.toString()
}

frontend {
    nodeDistributionProvided.set(false)
    nodeVersion.set("22.11.0")
    nodeInstallDirectory.set(project.layout.projectDirectory.dir("node"))
    corepackVersion.set("latest")

    installScript.set("install")
    assembleScript.set("run build")

    packageJsonDirectory.set(project.layout.projectDirectory.dir("src/main/frontend"))
    cacheDirectory.set(project.layout.projectDirectory.dir(".frontend-gradle-plugin"))

}

tasks.register<Copy>("processFrontendResources") {
    val frontendBuildDir = project.layout.projectDirectory.dir("src/main/frontend/dist/spa")
    val frontendResourcesDir = project.layout.buildDirectory.dir("resources/main/static")

    dependsOn(":assembleFrontend")
    from(frontendBuildDir)
    into(frontendResourcesDir)
}

tasks.named<Task>("processResources") {
    dependsOn("processFrontendResources")
}

configure<VersioningExtension> {
    /**
     * Add GitHub CI branch name environment variable
     */
    branchEnv = listOf("GITHUB_REF_NAME")
}

extra {
    val versioning: VersioningExtension = extensions.getByName<VersioningExtension>("versioning")
    val branch = versioning.info.branch.replace("/", "-")
    val shortCommit = versioning.info.commit.take(8)

    project.extra["build.revision"] = versioning.info.commit
    project.extra["build.revision.abbreviated"] = shortCommit
    project.extra["build.branch"] = branch

    val containerImageName = "holly394/${project.name}"
    val containerImageTags = mutableSetOf(shortCommit, branch)
    if (branch.startsWith("v")) {
        containerImageTags.add("stable")
    }

    val registryImageName = "ghcr.io/${containerImageName}"
    val registryImageTags = containerImageTags.map { "ghcr.io/${containerImageName}:$it" }.toMutableList()

    project.extra["docker.image.name"] = registryImageName
    project.extra["docker.image.version"] = branch
    project.extra["docker.image.tags"] = registryImageTags

}

tasks.withType<BootRun> {
    jvmArgs(
        arrayOf(
            "-Dspring.config.additional-location=optional:file:/workspace/application.yaml",
            "-Dsun.jnu.encoding=UTF-8",
            "-Dfile.encoding=UTF-8",
        )
    )
}


tasks.withType<BootBuildImage> {

    docker.publishRegistry.url = "ghcr.io"

    docker.publishRegistry.username = System.getenv("USERNAME") ?: "INVALID_USER"
    docker.publishRegistry.password = System.getenv("GITHUB_TOKEN") ?: "INVALID_PASSWORD"

    builder = "paketobuildpacks/builder-jammy-buildpackless-tiny"

    buildpacks = listOf(
        "paketobuildpacks/environment-variables",
        "paketobuildpacks/adoptium",
        "paketobuildpacks/java"
    )

    imageName = project.extra["docker.image.name"] as String
    version = project.extra["docker.image.version"] as String
    tags = project.extra["docker.image.tags"] as List<String>
    createdDate = "now"

    environment = mapOf(
        "BPL_JVM_CDS_ENABLED" to "true",
        "BP_JVM_CDS_ENABLED" to "true",
        "BP_JVM_VERSION" to "24",
        "BPE_LANG" to "en_US.UTF-8",
        "BPE_LANGUAGE" to "LANGUAGE=en_US:en",
        "BPE_LC_ALL" to "en_US.UTF-8",
        "BPE_APPEND_JAVA_OPTS" to "-Xmx256m -Xms128m"
    )
}
