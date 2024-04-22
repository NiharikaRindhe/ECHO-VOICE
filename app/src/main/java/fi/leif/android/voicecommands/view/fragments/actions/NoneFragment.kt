package fi.leif.android.voicecommands.view.fragments.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fi.leif.android.voicecommands.R
import fi.leif.voicecommands.Command

class NoneFragment : ActionFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, parent, savedInstanceState)
        return inflater.inflate(R.layout.action_none, parent, false)
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun getParameters(): Map<String, String> {
        return HashMap()
    }

    override fun setUpdateMode(command: Command) {
    }
}