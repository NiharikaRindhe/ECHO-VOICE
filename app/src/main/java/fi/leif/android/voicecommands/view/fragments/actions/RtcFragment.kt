package fi.leif.android.voicecommands.view.fragments.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import fi.leif.android.voicecommands.R
import fi.leif.android.voicecommands.databinding.ActionRtcBinding
import fi.leif.android.voicecommands.mappers.RtcTypeMapper
import fi.leif.voicecommands.ParameterKeys
import fi.leif.voicecommands.RtcType

@AndroidEntryPoint
abstract class RtcFragment: ActionFragment() {

    private lateinit var binding: ActionRtcBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, parent, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.action_rtc, parent, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRtcTypes()
        handleNoContacts()
    }

    protected open fun initRtcTypes() {
        val rtcTextView: AutoCompleteTextView = requireView().findViewById(R.id.rtc_type)
        rtcTextView.threshold = Int.MAX_VALUE
    }

    private fun handleNoContacts() {
        viewModel.noRtcContacts.observe(viewLifecycleOwner) {
            if(it) {
                val rtcTextView: AutoCompleteTextView = requireView().findViewById(R.id.rtc_type)
                rtcTextView.isEnabled = false
                rtcTextView.isFocusable = false
                rtcTextView.setText(RtcTypeMapper.getRtcTypeName(context, RtcType.MESSAGE))
            }
        }
    }

    override suspend fun fetchValues() {
        viewModel.setRtcTypeNames(RtcTypeMapper.getAllRtcTypeNames(context))
    }

    override fun setValues() {
        val contactId: String? = getParamVal(ParameterKeys.CONTACT_ID)
        contactId?.let { viewModel.setSelectedContactById(it) }

        val rtcValue: String? = getParamVal(ParameterKeys.RTC_TYPE)
        rtcValue?.let { viewModel.setSelectedRtcTypeName(RtcTypeMapper.getRtcTypeName(context,it)) }
            ?: RtcTypeMapper.getRtcTypeName(context, RtcType.MESSAGE)
    }

    override fun getParameters(): Map<String, String> {
        val params: HashMap<String, String> = HashMap()
        viewModel.selectedContact.get()?.let {
            params[ParameterKeys.CONTACT_NAME.toString()] = it.name
            params[ParameterKeys.CONTACT_PHONE.toString()] = it.phone
            params[ParameterKeys.CONTACT_ID.toString()] = it.id
        }
        params[ParameterKeys.RTC_TYPE.toString()] = RtcTypeMapper
            .getRtcType(context, viewModel.selectedRtcTypeName.value).toString()
        return params
    }

    override fun isValid(): Boolean {
        val contactsView: AutoCompleteTextView = requireView().findViewById(R.id.contacts)
        if(RtcTypeMapper.getRtcType(context, viewModel.selectedRtcTypeName.value) == RtcType.MESSAGE &&
            viewModel.selectedContact.get() == null) {
            contactsView.error = getString(R.string.error_rtc_message_recipient_empty)
            return false
        }
        return true
    }
}