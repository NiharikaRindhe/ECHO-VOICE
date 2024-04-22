package fi.leif.android.voicecommands.view.fragments.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textfield.TextInputEditText
import fi.leif.android.voicecommands.R
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.ParameterKeys

class MediaFragment: ActionFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, parent, savedInstanceState)
        return inflater.inflate(R.layout.action_media, parent, false)
    }

    override fun isValid(): Boolean {
        return true // No validation
    }

    override fun getParameters(): Map<String, String> {
        val input: TextInputEditText = requireView().findViewById(R.id.search_media_input)
        val params: HashMap<String, String> = HashMap()
        if(input.text.toString().trim().isEmpty()) return params
        params[ParameterKeys.SEARCH_VALUE.toString()] = input.text.toString()
        return params
    }

    override fun setUpdateMode(command: Command) {
        val input: TextInputEditText = requireView().findViewById(R.id.search_media_input)
        input.setText(getParamVal(command, ParameterKeys.SEARCH_VALUE))
    }
}