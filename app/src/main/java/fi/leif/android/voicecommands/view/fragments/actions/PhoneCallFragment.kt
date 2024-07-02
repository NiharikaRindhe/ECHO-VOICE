package fi.leif.android.voicecommands.view.fragments.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import fi.leif.android.voicecommands.R
import fi.leif.android.voicecommands.databinding.ActionPhoneCallBinding
import fi.leif.voicecommands.ParameterKeys

@AndroidEntryPoint
class PhoneCallFragment: ActionFragment() {

    private lateinit var binding: ActionPhoneCallBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, parent, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.action_phone_call,
            parent, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override suspend fun fetchValues() {
        viewModel.fetchPhoneContacts()
    }

    override fun setValues() {
        val contactId: String? = getParamVal(ParameterKeys.CONTACT_ID)
        contactId?.let { viewModel.setSelectedContactById(it) }
    }

    override fun getParameters(): Map<String, String> {
        val params: HashMap<String, String> = HashMap()
        viewModel.selectedContact.get()?.let {
            params[ParameterKeys.CONTACT_NAME.toString()] = it.name
            params[ParameterKeys.CONTACT_PHONE.toString()] = it.phone
            params[ParameterKeys.CONTACT_ID.toString()] = it.id
        }
        return params
    }

    override fun isValid(): Boolean {
        return true // No validation
    }

}