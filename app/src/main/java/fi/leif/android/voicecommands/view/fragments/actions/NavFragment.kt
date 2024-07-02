package fi.leif.android.voicecommands.view.fragments.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import fi.leif.android.voicecommands.R
import fi.leif.android.voicecommands.databinding.ActionNavBinding
import fi.leif.voicecommands.ParameterKeys

@AndroidEntryPoint
class NavFragment: ActionFragment() {

    private lateinit var binding: ActionNavBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, parent, savedInstanceState)
        binding = DataBindingUtil.inflate(inflater, R.layout.action_nav, parent, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun isValid(): Boolean {
        return true // No validation
    }

    override fun getParameters(): Map<String, String> {
        val input: TextInputEditText = requireView().findViewById(R.id.destination_input)
        val params: HashMap<String, String> = HashMap()
        if(input.text.toString().trim().isEmpty()) return params
        params[ParameterKeys.DESTINATION.toString()] = input.text.toString()
        return params
    }

    override suspend fun fetchValues() {
    }

    override fun setValues() {
        viewModel.setDestination(getParamVal(ParameterKeys.DESTINATION))
    }
}