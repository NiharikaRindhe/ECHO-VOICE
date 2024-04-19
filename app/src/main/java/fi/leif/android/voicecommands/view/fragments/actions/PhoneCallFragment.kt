package fi.leif.android.voicecommands.view.fragments.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fi.leif.android.voicecommands.R
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.ParameterKeys

class PhoneCallFragment: RtcFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, parent, savedInstanceState)
        return inflater.inflate(R.layout.action_phone_call, parent, false)
    }

    override fun getParameters(): Map<String, String> {
        val params: HashMap<String, String> = HashMap()
        viewModel.selectedContact.value?.let {
            params[ParameterKeys.CONTACT_NAME.toString()] = it.name
            params[ParameterKeys.CONTACT_PHONE.toString()] = it.phone
            params[ParameterKeys.CONTACT_ID.toString()] = it.id
        }
        return params
    }

    override fun fetchContacts() {
        viewModel.fetchPhoneContacts()
    }

    override fun setUpdateMode(command: Command) {
        if(command.action == Action.PHONE_CALL) {
            val contactValue: String? = getParamVal(command, ParameterKeys.CONTACT_ID)
            contactValue?.let { viewModel.setSelectedContactByValue(it) }
        } else {
            viewModel.setSelectedContact(null)
        }
    }

    override fun initRtcTypes() {}
}