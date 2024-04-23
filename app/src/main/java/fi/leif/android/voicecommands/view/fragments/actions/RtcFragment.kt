package fi.leif.android.voicecommands.view.fragments.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import fi.leif.android.voicecommands.R
import fi.leif.android.voicecommands.mappers.RtcTypeMapper
import fi.leif.voicecommands.ParameterKeys
import fi.leif.voicecommands.RtcType

abstract class RtcFragment: ActionFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, parent, savedInstanceState)
        return inflater.inflate(R.layout.action_rtc, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRtcTypes()
        initContacts()
        handleNoContacts()
    }

    private fun handleNoContacts() {
        viewModel.noRtcContacts.observe(viewLifecycleOwner) {
            if(it) {
                val rtcTextView: AutoCompleteTextView = requireView().findViewById(R.id.rtc_type)
                rtcTextView.isEnabled = false
                rtcTextView.isFocusable = false
                initCommand()
            }
        }
    }

    protected open fun initRtcTypes() {
        val rtcTextView: AutoCompleteTextView = requireView().findViewById(R.id.rtc_type)
        rtcTextView.threshold = Int.MAX_VALUE

        val rtcTypes: Array<String> = RtcTypeMapper.getAllRtcTypeNames(requireContext())
        rtcTextView.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, rtcTypes))

        rtcTextView.setOnItemClickListener { _, _, pos, _ ->
            viewModel.setSelectedRtcType(RtcTypeMapper.getRtcTypeAt(pos))
        }

        viewModel.selectedRtcType.observe(viewLifecycleOwner) {
            rtcTextView.setText(RtcTypeMapper.getRtcTypeName(requireContext(), it))
        }
    }

    private fun initContacts() {
        // Contacts dropdown
        val contactsView: AutoCompleteTextView = requireView().findViewById(R.id.contacts)
        contactsView.threshold = Int.MAX_VALUE
        // Selection changed
        contactsView.setOnItemClickListener { _, _, pos, _ ->
            if(pos == 0) viewModel.setSelectedContact(null)
            else {
                contactsView.error = null // Empty previous error
                viewModel.setSelectedContactByPosition(pos - 1)
            } // First element in view is empty
        }
        // Update selected text
        viewModel.selectedContact.observe(viewLifecycleOwner) { contact ->
            if(contact == null) contactsView.setText("") // First element in view is empty
            else contactsView.setText(contact.name)
        }
        // Contacts arrives
        viewModel.contacts.observe(viewLifecycleOwner) { contacts ->
            val newContacts = listOf(null) + contacts // First element in view is empty
            val items = newContacts.map { cont -> cont?.name ?: "" }
                .toTypedArray()
                .sortedArray()
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
            contactsView.setAdapter(adapter)
        }
        // Init
        viewModel.setSelectedContact(null)
        // Fetch contacts
        fetchContacts()
    }

    override fun isValid(): Boolean {
        val contactsView: AutoCompleteTextView = requireView().findViewById(R.id.contacts)
        if(viewModel.selectedRtcType.value == RtcType.MESSAGE &&
            viewModel.selectedContact.value == null) {
            contactsView.error = getString(R.string.error_rtc_message_recipient_empty)
            return false
        }
        return true
    }

    override fun getParameters(): Map<String, String> {
        val params: HashMap<String, String> = HashMap()
        viewModel.selectedContact.value?.let {
            params[ParameterKeys.CONTACT_NAME.toString()] = it.name
            params[ParameterKeys.CONTACT_PHONE.toString()] = it.phone
            params[ParameterKeys.CONTACT_ID.toString()] = it.id
        }
        viewModel.selectedRtcType.value?.let {
            // Ignoring setting default rtc type due to Executor.executeWithPackageName()
            // not launching getLaunchIntentForPackage when empty text - TODO: This is bit crap
            if(it != RtcType.AUDIO_CALL)
                params[ParameterKeys.RTC_TYPE.toString()] = it.toString()
        }
        return params
    }

    abstract fun fetchContacts()
}