package eu.kanade.tachiyomi.ui.manga.dedupe

import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import eu.kanade.tachiyomi.databinding.DedupeScanlatorsSettingsDialogBinding
import tachiyomi.domain.manga.model.SortedScanlator
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Stable
class DedupeScanlatorsSettingsState(
    private val context: Context,
    private val onDismissRequest: () -> Unit,
    private val onPositiveClick: (List<String>) -> Unit
) : SortedDedupeScanlatorAdapter.SortedDedupeScanlatorsItemListener, DedupeScanlatorAdapter.DedupeScanlatorsItemListener {
    private var localScanlators: List<String> by  mutableStateOf(emptyList())
    private var localScanlatorsAdapter: DedupeScanlatorAdapter? by mutableStateOf(null)
    private var localSortedScanlators: Set<SortedScanlator> by mutableStateOf(emptySet())
    private var localSortedScanlatorsAdapter: SortedDedupeScanlatorAdapter? by mutableStateOf(null)


    fun onViewCreated(
        context: Context,
        binding: DedupeScanlatorsSettingsDialogBinding,
        scanlators: List<String>,
        sortedScanlators: Set<SortedScanlator>,
    ) {
        localScanlators = scanlators
        localScanlatorsAdapter = DedupeScanlatorAdapter(this)
        localSortedScanlators = sortedScanlators
        localSortedScanlatorsAdapter = SortedDedupeScanlatorAdapter(this)

        binding.scanlators.adapter = localScanlatorsAdapter
        binding.scanlators.layoutManager = LinearLayoutManager(context)

        binding.sortedScanlators.adapter = localSortedScanlatorsAdapter
        binding.sortedScanlators.layoutManager = LinearLayoutManager(context)

        localScanlatorsAdapter?.updateDataSet(
            localScanlators.sortedBy { it }.map {
                DedupeScanlatorItem(it)
            }
        )

        localSortedScanlatorsAdapter?.isHandleDragEnabled = true

        localSortedScanlatorsAdapter?.updateDataSet(
            localSortedScanlators.sortedBy { it.rank }.map {
                SortedDedupeScanlatorItem(it.scanlator)
            },
        )
    }

    override fun onItemReleased(position: Int) {
        val sortScanlatorAdapter = localSortedScanlatorsAdapter ?: return
        sortScanlatorAdapter.notifyItemChanged(position)
        Log.i("", localSortedScanlators.toString())
    }

    override fun onRemoveClicked(position: Int) {

    }

    override fun onAddClicked(position: Int) {
        val scanlator = localScanlatorsAdapter?.getItem(position) ?: return

        localSortedScanlatorsAdapter?.notifyItemInserted(localSortedScanlatorsAdapter?.itemCount ?: 0)
        localSortedScanlatorsAdapter?.addItem(SortedDedupeScanlatorItem(scanlator.scanlator))
        localScanlatorsAdapter?.removeItem(position)
        localScanlatorsAdapter?.notifyItemRemoved(position)


    }

    fun onPositiveButtonClick() {
        onPositiveClick(listOfNotNull())
        onDismissRequest()
    }
}

@Composable
fun DedupeScanlatorsSettingsDialog(
    onDismissRequest: () -> Unit,
    sortedScanlators: Set<SortedScanlator>,
    scanlators: Set<String>,
    onPositiveClick: (List<String>) -> Unit,
) {
    val context = LocalContext.current
    val state = remember {
        DedupeScanlatorsSettingsState(context, onDismissRequest, onPositiveClick)
    }
    val rememberedScanlators by remember { mutableStateOf(scanlators.toList()) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = state::onPositiveButtonClick) {
                Text(stringResource(MR.strings.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(MR.strings.action_cancel))
            }
        },
        text = {
            Column (
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                AndroidView(
                    factory = {
                        factoryContext ->
                        val binding = DedupeScanlatorsSettingsDialogBinding.inflate(LayoutInflater.from(factoryContext))
                        state.onViewCreated(factoryContext, binding, rememberedScanlators, sortedScanlators)
                        binding.root
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = true
        )
    )
}
