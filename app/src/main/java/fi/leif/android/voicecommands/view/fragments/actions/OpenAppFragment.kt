package fi.leif.android.voicecommands.view.fragments.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import fi.leif.android.voicecommands.R
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.ParameterKeys

class OpenAppFragment: ActionFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, parent, savedInstanceState)
        return inflater.inflate(R.layout.action_open_app, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initApps()
    }

    private fun initApps() {
        // Apps dropdown
        val appsView: AutoCompleteTextView = requireView().findViewById(R.id.apps)
        appsView.threshold = Int.MAX_VALUE
        // Selection changed
        appsView.setOnItemClickListener { _, _, pos, _ ->
            viewModel.setSelectedAppByPosition(pos)
        }
        // Update selected text
        viewModel.selectedApp.observe(viewLifecycleOwner) { appsView.setText(it.name) }
        // App list arrives
        viewModel.apps.observe(viewLifecycleOwner) { apps ->
            val items = apps.map { app -> app.name }.toTypedArray().sortedArray()
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
            appsView.setAdapter(adapter)
            // Default to first app in list
            val selectedApp = viewModel.selectedApp.value
            if(selectedApp == null) viewModel.setSelectedAppByPosition(0)
        }
        // Fetch contacts
        viewModel.fetchApps()
    }

    override fun setUpdateMode(command: Command) {
        val appValue: String? = getParamVal(command, ParameterKeys.APP_PACKAGE)
        appValue?.let {
            viewModel.setSelectedAppByValue(it)
        }
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun getParameters(): Map<String, String> {
        val params: HashMap<String, String> = HashMap()
        val selectedApp = viewModel.selectedApp.value
        selectedApp?.let {
            params[ParameterKeys.APP_NAME.toString()] = it.name
            params[ParameterKeys.APP_PACKAGE.toString()] = it.pkg
        }
        return params
    }

}