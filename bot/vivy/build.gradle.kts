plugins {
    id("bogus.kordex-bot")
}

application {
    mainClass.set("bogus.bot.vivy.AppKt")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":extension:music"))
    implementation(project(":extension:about"))
}