package fi.leif.android.voicecommands.view

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import fi.leif.android.voicecommands.R
import fi.leif.android.voicecommands.databinding.SettingsActivityBinding
import fi.leif.android.voicecommands.viewmodel.Constants
import fi.leif.android.voicecommands.viewmodel.EditMode
import fi.leif.android.voicecommands.viewmodel.SettingsViewModel
import fi.leif.android.voicecommands.viewmodel.ValidationError

class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        // Bind viewmodel to layout xml
        val binding: SettingsActivityBinding = DataBindingUtil.setContentView(
            this, R.layout.settings_activity)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Extra languages dropdown
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, emptyArray<String>())
        val extraLangs: AutoCompleteTextView = findViewById(R.id.extra_language)
        extraLangs.threshold = Int.MAX_VALUE
        extraLangs.setAdapter(adapter)

        // Selection changes
        extraLangs.setOnItemClickListener { _, _, pos, _ ->
            viewModel.setLanguageCodeByPosition(pos)
        }

        // Extra languages list
        viewModel.supportedLanguages.observe(this) {
            extraLangs.setAdapter(
                ArrayAdapter(this,android.R.layout.simple_list_item_1, it.toTypedArray())
            )
            val selectedIndex = viewModel.getSettingsExtraLanguageIndex()
            extraLangs.setText(it[selectedIndex])
        }

        // Edit commands button
        val editCmdsBtn: Button = findViewById(R.id.edit_commands)
        editCmdsBtn.setOnClickListener{
            startActivity(Intent(this, ListCommandsActivity::class.java))
        }

        // Edit default command button
        val editDefaultCmdBtn: Button = findViewById(R.id.edit_default_command)
        editDefaultCmdBtn.setOnClickListener{
            val intent = Intent(this, EditCommandActivity::class.java)
            intent.putExtra(Constants.KEY_EDIT_MODE, EditMode.DEFAULT_COMMAND.name)
            startActivity(Intent(intent))
        }

        // RMS DB input & layout
        val rmsLayout: TextInputLayout = findViewById(R.id.rms_db_layout)
        rmsLayout.hint = getString(R.string.hint_rms_db, viewModel.recMaxRmsDb.value)

        // Validation error
        val rmsInput: TextInputEditText = findViewById(R.id.rms_db_input)
        viewModel.validationError.observe(this) {
            if(it == ValidationError.INVALID_RMS_DB) {
                rmsInput.error = getString(R.string.error_rms_db)
            }
        }

        // Back button
        val topBar: MaterialToolbar = findViewById(R.id.top_bar)
        topBar.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}