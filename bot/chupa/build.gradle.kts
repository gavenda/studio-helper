plugins {
    id("bogus.kordex-bot")
}

application {
    mainClass.set("bogus.bot.chupa.AppKt")
}

dependencies {
    implementation(project(":extension:about"))
    implementation(project(":extension:counter"))
}