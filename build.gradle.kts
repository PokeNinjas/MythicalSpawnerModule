import com.mythicalnetwork.gradle.Dependencies
import com.mythicalnetwork.gradle.Versions
import com.mythicalnetwork.gradle.ProjectInfo
import com.mythicalnetwork.gradle.Repos

plugins {
    id("java")
    id("io.github.juuxel.loom-quiltflower")  version ("1.8.+")
    id("org.quiltmc.loom")  version("1.0.+")
    kotlin("jvm") version ("1.8.0")
    kotlin("kapt") version("1.8.20")
}

group = ProjectInfo.GROUP
version = ProjectInfo.VERSION
base {
    archivesName.set("MythicalSpawnerModule")
}

loom {
    mixin {
        defaultRefmapName.set("mixins.${project.name}.refmap.json")
    }
    interfaceInjection {
        enableDependencyInterfaceInjection.set(true)
    }
}

repositories {
    mavenCentral()
    for(repo in Repos.BASE){
        maven(url = repo)
    }
}


dependencies {
    minecraft("com.mojang:minecraft:${Versions.MINECRAFT}")
    mappings(loom.layered {
        mappings(Dependencies.QUILT_MAPPINGS)
        officialMojangMappings()
    })
    for(dep in Dependencies.CORE_DEPS){
        if(dep.equals("owo-lib")){
            modImplementation(dep)
        } else {
            include(dep)?.let {
                if(!dep.contains("owo-sentinel")){
                    modImplementation(it)
                }
            }
        }
        if(dep.contains("owo-lib")){
            annotationProcessor(dep)
            kapt(dep)
        }
    }
    testImplementation(Dependencies.JUNIT_JUPITER_API)
    testRuntimeOnly(Dependencies.JUNIT_JUPITER_ENGINE)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

