package fi.leif.android.voicecommands.view.fragments.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import fi.leif.android.voicecommands.R
import fi.leif.android.voicecommands.databinding.ActionOpenAppBinding
import fi.leif.voicecommands.ParameterKeys

@AndroidEntryPoint
class OpenAppFragment: ActionFragment() {

    private lateinit var binding: ActionOpenAppBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, parent, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.action_open_app, parent, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun setValues() {
        val appValue: String? = getParamVal(ParameterKeys.APP_PACKAGE)
        appValue?.let { viewModel.setSelectedAppByValue(it) }
    }

    override suspend fun fetchValues() {
        viewModel.fetchApps()
    }

    override fun isValid(): Boolean {
        return true // No validation
    }

    override fun getParameters(): Map<String, String> {
        val params: HashMap<String, String> = HashMap()
        val selectedApp = viewModel.selectedApp.get()
        selectedApp?.let {
            params[ParameterKeys.APP_NAME.toString()] = it.name
            params[ParameterKeys.APP_PACKAGE.toString()] = it.pkg
        }
        return params
    }

}