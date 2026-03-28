plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.compose)
	`maven-publish`
}

android {
	namespace = "sh.tablet.android.compose.filepicker"
	compileSdk {
		version = release(36) {
			minorApiLevel = 1
		}
	}

	defaultConfig {
		minSdk = 31

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	buildFeatures {
		compose = true
		buildConfig = true
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	publishing {
		singleVariant("release") {
			withSourcesJar()
		}
	}
}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.paging.runtime.ktx)

	implementation(libs.androidx.appcompat)

	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.lifecycle.viewmodel)
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.compose.material.icons)
	implementation(libs.androidx.compose.material.icons.extended)
	implementation(libs.androidx.paging.compose)

	implementation(libs.coil.compose)
	implementation(libs.coil.video)
	implementation(libs.accompanist)

	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.compose.ui.test.junit4)
	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	compilerOptions.freeCompilerArgs.add("-Xexplicit-backing-fields")
}
publishing {
	publications {
		create<MavenPublication>("release") {
			groupId = "io.jitpack"
			artifactId = "library"
			version = "0.0.1"
			afterEvaluate {
				from(components["release"])
			}
		}
	}
}