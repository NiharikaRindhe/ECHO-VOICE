package fi.leif.android.voicecommands.view.fragments.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import fi.leif.android.voicecommands.R
import fi.leif.android.voicecommands.databinding.ActionMediaBinding
import fi.leif.voicecommands.ParameterKeys

@AndroidEntryPoint
class MediaFragment: ActionFragment() {

    private lateinit var binding: ActionMediaBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, parent, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.action_media, parent, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
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

    override fun setValues() {
        viewModel.setSearchValue(getParamVal(ParameterKeys.SEARCH_VALUE))
    }

    override suspend fun fetchValues() {
    }

}