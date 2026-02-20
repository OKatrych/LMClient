@file:OptIn(ExperimentalHazeMaterialsApi::class)

package dev.olek.lmclient.presentation.ui.mobile.settings.licenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mikepenz.aboutlibraries.entity.Library
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import dev.olek.lmclient.presentation.components.settings.LicensesComponent
import dev.olek.lmclient.presentation.components.settings.author
import dev.olek.lmclient.presentation.components.settings.licenseName
import dev.olek.lmclient.presentation.theme.AppTheme
import dev.olek.lmclient.presentation.ui.mobile.common.lazyListHazeEffect
import dev.olek.lmclient.presentation.ui.mobile.common.topbar.CenteredTopBar
import dev.olek.lmclient.presentation.util.collectAsStateMultiplatform
import dev.olek.lmclient.presentation.util.plus
import lm_client.shared.generated.resources.Res
import lm_client.shared.generated.resources.licenses_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun LicensesScreen(component: LicensesComponent, modifier: Modifier = Modifier) {
    val state by component.state.collectAsStateMultiplatform()
    val hazeState = rememberHazeState()
    val listState = rememberLazyListState()
    val listTopPaddingPx = LocalDensity.current.run { LicensesListPadding.toPx() }

    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.surface,
        contentColor = Color.Unspecified,
        topBar = {
            CenteredTopBar(
                modifier = Modifier
                    .lazyListHazeEffect(
                        hazeState = hazeState,
                        hazeStyle = HazeMaterials.thin(AppTheme.colors.surface),
                        listState = listState,
                        listTopPaddingPx = listTopPaddingPx,
                    ),
                title = stringResource(Res.string.licenses_title),
                onBackClick = component::navigateBack,
            )
        },
        content = {
            ScreenContent(
                modifier = Modifier
                    .hazeSource(state = hazeState),
                paddingValues = it + PaddingValues(vertical = LicensesListPadding),
                state = state,
                listState = listState,
            )
        },
    )
}

@Composable
private fun ScreenContent(
    paddingValues: PaddingValues,
    state: LicensesComponent.State,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    var licenseState by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = paddingValues,
    ) {
        items(items = state.libraries, key = { it.uniqueId }) { library ->
            LibraryItem(
                library = library,
                onClick = {
                    licenseState = it.licenses.firstOrNull()?.licenseContent
                },
            )
        }
    }

    licenseState?.let { licenseContent ->
        Dialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { licenseState = null },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.colors.backgroundSecondary)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = licenseContent,
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.text,
                )
            }
        }
    }
}

@Composable
private fun LibraryItem(library: Library, onClick: (Library) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick(library) })
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = library.name,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.text,
            )
            Text(
                text = library.author,
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
            )
            Text(
                text = library.licenseName,
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
            )
        }
        library.artifactVersion?.let {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = it,
                style = AppTheme.typography.caption,
                color = AppTheme.colors.textSecondary,
            )
        }
    }
}

private val LicensesListPadding = 16.dp
