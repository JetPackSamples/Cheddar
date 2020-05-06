package co.adrianblan.storyfeed

import co.adrianblan.ui.node.NodeContext
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.Subcomponent
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Scope

@AssistedModule
@Module(includes = [AssistedInject_StoryFeedModule::class])
object StoryFeedModule

@StoryFeedScope
@Subcomponent(modules = [StoryFeedModule::class])
interface StoryFeedComponent {

    fun storyFeedNodeFactory(): StoryFeedNode.Factory

    @Subcomponent.Factory
    interface Factory {
        fun build(): StoryFeedComponent
    }
}

@Scope
@Retention
internal annotation class StoryFeedScope

class StoryFeedNodeBuilder
@Inject constructor(
    private val storyFeedComponentBuilder: StoryFeedComponent.Factory
) {
    fun build(
        nodeContext: NodeContext,
        listener: StoryFeedNode.Listener
    ): StoryFeedNode =
        storyFeedComponentBuilder
            .build()
            .storyFeedNodeFactory()
            .create(nodeContext, listener)
}