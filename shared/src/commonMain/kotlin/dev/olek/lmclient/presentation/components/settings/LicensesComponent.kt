package dev.olek.lmclient.presentation.components.settings

import com.arkivanov.decompose.ComponentContext
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import dev.olek.lmclient.presentation.components.util.StateFlowSharingTimeout
import dev.olek.lmclient.presentation.util.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import lm_client.shared.generated.resources.Res
import org.koin.core.component.KoinComponent

interface LicensesComponent {
    val state: StateFlow<State>

    fun navigateBack()

    data class State(val libraries: List<Library> = emptyList())
}

internal class LicensesComponentImpl(context: ComponentContext, private val onBackPressed: () -> Unit) :
    LicensesComponent,
    KoinComponent,
    ComponentContext by context {
    private val coroutineScope = coroutineScope(Dispatchers.Main.immediate)

    override val state: StateFlow<LicensesComponent.State> = flow {
        Libs
            .Builder()
            .withJson(Res.readBytes("files/aboutlibraries.json").decodeToString())
            .build()
            .libraries
            .sortedBy { it.name }
            .let {
                emit(LicensesComponent.State(libraries = it))
            }
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(StateFlowSharingTimeout),
        initialValue = LicensesComponent.State(),
    )

    override fun navigateBack() {
        onBackPressed()
    }
}

val Library.author: String
    get() = when {
        developers.isNotEmpty() -> developers.joinToString { it.name.toString() }
        else -> organization?.name ?: ""
    }

val Library.licenseName: String
    get() = licenses.firstOrNull()?.name ?: "Unknown license"
