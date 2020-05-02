package co.adrianblan.storynavigation

import androidx.compose.Composable
import androidx.ui.animation.Crossfade
import androidx.ui.layout.Stack
import co.adrianblan.ui.node.Node


data class StoryNavigationViewState(
    val activeNode: Node
)

@Composable
fun StoryNavigationView(viewState: StoryNavigationViewState) {
    Crossfade(viewState) {
        Stack {
            it.activeNode.render()
        }
    }
}