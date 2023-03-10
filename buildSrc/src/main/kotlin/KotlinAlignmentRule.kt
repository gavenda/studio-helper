import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule

class KotlinAlignmentRule : ComponentMetadataRule {
    override fun execute(ctx: ComponentMetadataContext) {
        ctx.details.run {
            if (id.group.startsWith("org.jetbrains.kotlin")) {
                belongsTo("org.jetbrains.kotlin:kotlin-bom:${id.version}", false)
            }
        }
    }
}