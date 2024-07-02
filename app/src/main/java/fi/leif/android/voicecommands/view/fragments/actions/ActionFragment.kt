package fi.leif.android.voicecommands.view.fragments.actions

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import fi.leif.android.voicecommands.viewmodel.ActionViewModel
import fi.leif.android.voicecommands.viewmodel.Constants
import fi.leif.android.voicecommands.viewmodel.EditMode
import fi.leif.voicecommands.ParameterKeys
import kotlinx.coroutines.launch

abstract class ActionFragment: Fragment() {

    protected val viewModel: ActionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            viewModel.fetchSettings()
            setCommand()
            fetchValues()
            setValues()
        }
    }

    private fun setCommand() {
        val sEditMode = arguments?.getString(Constants.KEY_EDIT_MODE)
        val editMode = sEditMode?.let { EditMode.valueOf(it) } ?: EditMode.NEW_COMMAND
        if(editMode == EditMode.DEFAULT_COMMAND) {
            viewModel.setDefaultCommand()
        } else if(editMode == EditMode.UPDATE_COMMAND) {
            val commandIndex: Int? = arguments?.getInt(Constants.KEY_COMMAND_INDEX)
            commandIndex?.let { viewModel.setCommandByIndex(it) }
        }
    }

    protected fun getParamVal(paramKey: ParameterKeys): String? {
        return viewModel.command.value?.parametersMap?.get(paramKey.toString())
    }

    abstract suspend fun fetchValues()
    abstract fun setValues()
    abstract fun isValid(): Boolean
    abstract fun getParameters(): Map<String, String>

}