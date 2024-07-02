package fi.leif.android.voicecommands.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import fi.leif.android.voicecommands.R
import fi.leif.android.voicecommands.databinding.SettingsActivityBinding
import fi.leif.android.voicecommands.viewmodel.SettingsViewModel
import fi.leif.android.voicecommands.viewmodel.ValidationError
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        // Bind viewmodel to layout xml
        val binding: SettingsActivityBinding = DataBindingUtil.setContentView(
            this, R.layout.settings_activity
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Fetch repositories
        lifecycleScope.launch { viewModel.fetchRepositories() }

        // Edit commands button
        val editCmdsBtn: Button = findViewById(R.id.edit_commands)
        editCmdsBtn.setOnClickListener {
            startActivity(Intent(this, ListCommandsActivity::class.java))
        }

        // RMS DB input & layout
        val rmsLayout: TextInputLayout = findViewById(R.id.rms_db_layout)
        rmsLayout.hint = getString(R.string.hint_rms_db, viewModel.recMaxRmsDb.value)

        // Back button
        val topBar: MaterialToolbar = findViewById(R.id.top_bar)
        topBar.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Validation error
        val rmsInput: TextInputEditText = findViewById(R.id.rms_db_input)
        viewModel.validationError.observe(this) {
            if (it == ValidationError.INVALID_RMS_DB) {
                rmsInput.error = getString(R.string.error_rms_db)
            }
        }
    }
}