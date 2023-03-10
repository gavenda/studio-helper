import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule

class KordAlignmentRule : ComponentMetadataRule {
    override fun execute(ctx: ComponentMetadataContext) {
        ctx.details.run {
            if (id.group.startsWith("dev.kord")) {
                belongsTo("dev.kord:kord-virtual-platform:${id.version}")
            }
        }
    }
}