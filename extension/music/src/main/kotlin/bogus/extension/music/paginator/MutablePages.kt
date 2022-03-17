package bogus.extension.music.paginator

import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.message.EmbedBuilder
import java.util.*

class MutablePages(defaultGroup: String = "") : Pages(defaultGroup) {

    fun clear(group: String = defaultGroup) {
        groups[group]?.clear()
    }

}

/**
 * Wrapping builder for easily creating paginators using DSL functions defined in the context classes.
 *
 * @param extension Extension the paginator is being created for
 * @param locale Locale to use for the paginator
 * @param defaultGroup Default page group, if any
 */
class MutablePaginatorBuilder(
    var locale: Locale? = null,
    val defaultGroup: String = ""
) {
    /** Pages container object. **/
    val pages: MutablePages = MutablePages(defaultGroup)

    /** Paginator owner, if only one person should be able to interact. **/
    var owner: UserBehavior? = null

    /** Paginator timeout, in seconds. When elapsed, the paginator will be destroyed. **/
    var timeoutSeconds: Long? = null

    /** Whether to keep the paginator content on Discord when the paginator is destroyed. **/
    var keepEmbed: Boolean = true

    /** Alternative switch button emoji, if needed. **/
    var switchEmoji: ReactionEmoji? = null

    /** Translations bundle to use for page groups, if any. **/
    var bundle: String? = null

    /** Add a page to [pages], using the default group. **/
    fun page(page: Page): Unit = pages.addPage(page)

    /** Add a page to [pages], using the given group. **/
    fun page(group: String, page: Page): Unit = pages.addPage(group, page)

    /** Add a page to [pages], using the default group. **/
    fun page(
        bundle: String? = null,
        builder: suspend EmbedBuilder.() -> Unit
    ): Unit =
        page(Page(builder = builder, bundle = bundle))

    /** Add a page to [pages], using the given group. **/
    fun page(
        group: String,
        bundle: String? = null,
        builder: suspend EmbedBuilder.() -> Unit
    ): Unit =
        page(group, Page(builder = builder, bundle = bundle))
}
