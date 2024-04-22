package fi.leif.android.voicecommands.view.fragments.actions

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import fi.leif.android.voicecommands.viewmodel.ActionViewModel
import fi.leif.android.voicecommands.viewmodel.Constants
import fi.leif.android.voicecommands.viewmodel.EditMode
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.ParameterKeys

abstract class ActionFragment: Fragment() {

    protected val viewModel: ActionViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCommand()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.command.observe(viewLifecycleOwner) {  setUpdateMode(it) }
    }

    protected fun setCommand() {
        val sEditMode = arguments?.getString(Constants.KEY_EDIT_MODE)
        val editMode = sEditMode?.let { EditMode.valueOf(it) } ?: EditMode.NEW_COMMAND
        if(editMode == EditMode.DEFAULT_COMMAND) {
            viewModel.setDefaultCommand()
        } else if(editMode == EditMode.UPDATE_COMMAND) {
            val commandIndex: Int? = arguments?.getInt(Constants.KEY_COMMAND_INDEX)
            commandIndex?.let { viewModel.setCommand(it) }
        }
    }

    protected fun getParamVal(command: Command, paramKey: ParameterKeys): String? {
        return command.parametersMap[paramKey.toString()]
    }

    abstract fun isValid(): Boolean
    abstract fun getParameters(): Map<String, String>
    abstract fun setUpdateMode(command: Command)

}