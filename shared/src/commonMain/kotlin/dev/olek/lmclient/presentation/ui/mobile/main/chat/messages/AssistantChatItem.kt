package dev.olek.lmclient.presentation.ui.mobile.main.chat.messages

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.LocalImageTransformer
import com.mikepenz.markdown.compose.LocalMarkdownAnimations
import com.mikepenz.markdown.compose.LocalMarkdownAnnotator
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownComponents
import com.mikepenz.markdown.compose.LocalMarkdownDimens
import com.mikepenz.markdown.compose.LocalMarkdownExtendedSpans
import com.mikepenz.markdown.compose.LocalMarkdownInlineContent
import com.mikepenz.markdown.compose.LocalMarkdownPadding
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.LocalReferenceLinkHandler
import com.mikepenz.markdown.compose.MarkdownElement
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.elements.MarkdownCheckBox
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import com.mikepenz.markdown.model.ImageTransformer
import com.mikepenz.markdown.model.MarkdownAnimations
import com.mikepenz.markdown.model.MarkdownAnnotator
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownDimens
import com.mikepenz.markdown.model.MarkdownExtendedSpans
import com.mikepenz.markdown.model.MarkdownInlineContent
import com.mikepenz.markdown.model.MarkdownPadding
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.NoOpImageTransformerImpl
import com.mikepenz.markdown.model.ReferenceLinkHandler
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownDimens
import com.mikepenz.markdown.model.markdownExtendedSpans
import com.mikepenz.markdown.model.markdownInlineContent
import com.mikepenz.markdown.model.markdownPadding
import dev.olek.lmclient.data.models.LMClientError
import dev.olek.lmclient.presentation.components.main.chatitems.ChatItemsListState.ChatItem
import dev.olek.lmclient.presentation.components.main.chatitems.ChatItemsListState.ChatItem.ChatItemContent
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.extensions.toLocalizedMessage
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.ic_copy
import lm_client.shared.generated.resources.ic_error
import lm_client.shared.generated.resources.ic_retry
import lm_client.shared.generated.resources.ic_share
import lm_client.shared.generated.resources.messages_list_action_row_copy_desc
import lm_client.shared.generated.resources.messages_list_action_row_retry_desc
import lm_client.shared.generated.resources.messages_list_action_row_share_desc
import lm_client.shared.generated.resources.messages_list_error_retry_desc
import lm_client.shared.generated.resources.messages_list_error_title
import org.intellij.markdown.ast.ASTNode
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Assistant answer can be huge, let's split it by smaller elements
 */
internal fun LazyListScope.assistantChatItem(
    item: ChatItem.AssistantItem,
    onRetry: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
) {
    val finishedGeneration = item.finishReason != null || item.error != null
    if (item.finishReason != null && item.error == null) {
        item(key = "actions_${item.id}") {
            ActionsRow(onRetry = onRetry, onCopy = onCopy, onShare = onShare)
        }
    }
    if (item.error != null) {
        item(key = "error_${item.id}") {
            ErrorCard(
                modifier = Modifier.padding(top = 16.dp),
                error = item.error,
                onRetry = onRetry,
            )
        }
    }
    when (val content = item.content) {
        is ChatItemContent.MarkdownContent -> {
            items(
                items = content.markdownState.node.children
                    .reversed(),
                key = { "${item.id}_md_element_${it.startOffset}" },
            ) { node ->
                MarkdownChatElement(
                    node = node,
                    content = content.markdownState.content,
                    referenceLinkHandler = content.markdownState.referenceLinkHandler,
                )
            }
        }

        is ChatItemContent.TextContent -> {
            item(key = item.id) {
                TextChatElement(
                    content = content,
                    finishedGeneration = finishedGeneration,
                )
            }
        }

        is ChatItemContent.AudioContent -> TODO("Not yet supported")
        is ChatItemContent.ImageContent -> TODO("Not yet supported")
    }
}

@Composable
private fun MarkdownChatElement(
    node: ASTNode,
    content: String,
    referenceLinkHandler: ReferenceLinkHandler,
    colors: MarkdownColors = appMarkdownColors(),
    typography: MarkdownTypography = appMarkdownTypography(),
    padding: MarkdownPadding = markdownPadding(),
    dimens: MarkdownDimens = markdownDimens(),
    imageTransformer: ImageTransformer = NoOpImageTransformerImpl(),
    annotator: MarkdownAnnotator = markdownAnnotator(),
    extendedSpans: MarkdownExtendedSpans = markdownExtendedSpans(),
    inlineContent: MarkdownInlineContent = markdownInlineContent(),
    components: MarkdownComponents = markdownComponents(checkbox = {
        MarkdownCheckBox(it.content, it.node, it.typography.text)
    }),
    animations: MarkdownAnimations = markdownAnimations(),
) {
    SelectionContainer {
        CompositionLocalProvider(
            LocalReferenceLinkHandler provides referenceLinkHandler,
            LocalMarkdownPadding provides padding,
            LocalMarkdownDimens provides dimens,
            LocalMarkdownColors provides colors,
            LocalMarkdownTypography provides typography,
            LocalImageTransformer provides imageTransformer,
            LocalMarkdownAnnotator provides annotator,
            LocalMarkdownExtendedSpans provides extendedSpans,
            LocalMarkdownInlineContent provides inlineContent,
            LocalMarkdownComponents provides components,
            LocalMarkdownAnimations provides animations,
        ) {
            MarkdownElement(node, components, content)
        }
    }
}

@Composable
private fun TextChatElement(content: ChatItemContent.TextContent, finishedGeneration: Boolean) {
    // Set `shouldAnimateSize` based on the initial `finishedGeneration` state.
    // This ensures that if a message begins generating, `animateContentSize`
    // remains active throughout its entire generation, including animating
    // the final content update even after `finishedGeneration` becomes true.
    val shouldAnimateSize by remember { mutableStateOf(finishedGeneration) }
    SelectionContainer {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .animateChatText(shouldAnimateSize),
            text = content.text,
            style = AppTheme.typography.body,
            color = AppTheme.colors.text,
        )
    }
}

@Composable
private fun ActionsRow(onRetry: () -> Unit, onCopy: () -> Unit, onShare: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCopy) {
            Icon(
                painter = painterResource(Res.drawable.ic_copy),
                tint = AppTheme.colors.textSecondary,
                contentDescription = stringResource(Res.string.messages_list_action_row_copy_desc),
            )
        }
        IconButton(onClick = onShare) {
            Icon(
                painter = painterResource(Res.drawable.ic_share),
                tint = AppTheme.colors.textSecondary,
                contentDescription = stringResource(Res.string.messages_list_action_row_share_desc),
            )
        }
        IconButton(onClick = onRetry) {
            Icon(
                painter = painterResource(Res.drawable.ic_retry),
                tint = AppTheme.colors.textSecondary,
                contentDescription = stringResource(Res.string.messages_list_action_row_retry_desc),
            )
        }
    }
}

@Composable
private fun ErrorCard(error: LMClientError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = AppTheme.colors.backgroundError, shape = AppTheme.shapes.card)
            .border(width = 1.dp, color = AppTheme.colors.error, shape = AppTheme.shapes.card),
    ) {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier
                    .size(24.dp),
                painter = painterResource(Res.drawable.ic_error),
                tint = AppTheme.colors.error,
                contentDescription = null,
            )
        }
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.messages_list_error_title),
                    style = AppTheme.typography.bodySemiBold,
                    color = AppTheme.colors.error,
                )
                IconButton(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = onRetry,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_retry),
                        tint = AppTheme.colors.error,
                        contentDescription = stringResource(Res.string.messages_list_error_retry_desc),
                    )
                }
            }
            Text(
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp),
                text = error.toLocalizedMessage(),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.error,
            )
        }
    }
}

private fun Modifier.animateChatText(enabled: Boolean) = then(
    if (enabled) {
        Modifier.animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    } else {
        Modifier
    },
)

@Composable
private fun appMarkdownColors(
    text: Color = AppTheme.colors.text,
    codeBackground: Color = AppTheme.colors.background,
    inlineCodeBackground: Color = codeBackground,
    dividerColor: Color = AppTheme.colors.surface,
    tableBackground: Color = AppTheme.colors.background,
): MarkdownColors = DefaultMarkdownColors(
    text = text,
    codeBackground = codeBackground,
    inlineCodeBackground = inlineCodeBackground,
    dividerColor = dividerColor,
    tableBackground = tableBackground,
)

@Composable
private fun appMarkdownTypography(
    h1: TextStyle = AppTheme.typography.titleBold,
    h2: TextStyle = AppTheme.typography.title,
    h3: TextStyle = AppTheme.typography.title,
    h4: TextStyle = AppTheme.typography.title,
    h5: TextStyle = AppTheme.typography.title,
    h6: TextStyle = AppTheme.typography.title,
    text: TextStyle = AppTheme.typography.body,
    code: TextStyle = AppTheme.typography.body.copy(fontFamily = FontFamily.Monospace),
    inlineCode: TextStyle = text.copy(fontFamily = FontFamily.Monospace),
    quote: TextStyle = AppTheme.typography.body.plus(SpanStyle(fontStyle = FontStyle.Italic)),
    paragraph: TextStyle = AppTheme.typography.body,
    ordered: TextStyle = AppTheme.typography.body,
    bullet: TextStyle = AppTheme.typography.body,
    list: TextStyle = AppTheme.typography.body,
    textLink: TextLinkStyles = TextLinkStyles(
        style = AppTheme.typography.body
            .copy(
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
            ).toSpanStyle(),
    ),
    table: TextStyle = text,
): MarkdownTypography = DefaultMarkdownTypography(
    h1 = h1,
    h2 = h2,
    h3 = h3,
    h4 = h4,
    h5 = h5,
    h6 = h6,
    text = text,
    quote = quote,
    code = code,
    inlineCode = inlineCode,
    paragraph = paragraph,
    ordered = ordered,
    bullet = bullet,
    list = list,
    textLink = textLink,
    table = table,
)
