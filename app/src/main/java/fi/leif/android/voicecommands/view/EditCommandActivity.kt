package fi.leif.android.voicecommands.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import fi.leif.android.voicecommands.R
import fi.leif.android.voicecommands.mappers.ActionMapper
import fi.leif.android.voicecommands.view.fragments.actions.ActionFragment
import fi.leif.android.voicecommands.viewmodel.Constants
import fi.leif.android.voicecommands.viewmodel.EditCommandViewModel
import fi.leif.android.voicecommands.viewmodel.EditMode
import fi.leif.android.voicecommands.viewmodel.EditMode.*
import fi.leif.android.voicecommands.viewmodel.ValidationError

class EditCommandActivity : AppCompatActivity() {

    private val viewModel: EditCommandViewModel by viewModels()
    private lateinit var actionFragment: ActionFragment
    private lateinit var cmdLayout: TextInputLayout
    private lateinit var cmdInput: TextInputEditText
    private lateinit var chipGroup: ChipGroup
    private lateinit var editMode: EditMode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_command)

        // Set mode by parameters
        val sEditMode = intent.extras?.getString(Constants.KEY_EDIT_MODE)
        editMode = sEditMode?.let { EditMode.valueOf(it) } ?: NEW_COMMAND
        val commandIndex = intent.extras?.getInt(Constants.KEY_COMMAND_INDEX)
        viewModel.setMode(editMode, commandIndex)

        initCommandWordInput()
        initTexts()
        initCommandWordChips()
        initActions(commandIndex)
        initTitle()
        initBackButton()
        initSaveButton()
        setValidationErrorListener()
    }

    private fun initCommandWordInput() {
        cmdLayout = findViewById(R.id.command_layout)
        cmdInput = findViewById(R.id.command_input)

        // Hide if editing default command
        if(editMode == DEFAULT_COMMAND) {
            cmdLayout.visibility = View.GONE
            return
        }
        // Clear errors on text change
        cmdInput.doOnTextChanged { _,_,_,_ -> cmdLayout.error = null }
        // New command listener
        cmdLayout.setEndIconOnClickListener {
            val value = cmdInput.text.toString()
            if(viewModel.isValidKeyword(value)) {
                cmdInput.setText("")
                viewModel.addCommandWord(value)
            }
        }
    }

    private fun initTexts() {
        val info: TextView = findViewById(R.id.info)
        val keywordsTitle: TextView = findViewById(R.id.keywords_title)
        if((editMode == DEFAULT_COMMAND)) {
            info.visibility = View.VISIBLE
            keywordsTitle.visibility = View.GONE
        } else {
            info.visibility = View.GONE
            keywordsTitle.visibility = View.VISIBLE
        }
    }

    private fun initCommandWordChips() {
        chipGroup = findViewById(R.id.command_words)

        // Hide if editing default command
        if(editMode == DEFAULT_COMMAND) {
            chipGroup.visibility = View.GONE
            return
        }
        viewModel.commandWords.observe(this) { list ->
            chipGroup.removeAllViews()
            list.forEach { word ->
                val chip = Chip(this)
                chip.isCloseIconVisible = true
                chip.text = word
                chip.textSize = 20f
                chip.setOnCloseIconClickListener {
                    val c = it as Chip
                    viewModel.removeCommandWord(c.text.toString())
                }
                chipGroup.addView(chip)
            }
        }
    }

    private fun initActions(commandIndex: Int?) {
        val defaultCommand = (editMode == DEFAULT_COMMAND)
        val actions: Array<String> = ActionMapper.getAllActionNames(this, defaultCommand)
        val actionsView: AutoCompleteTextView = findViewById(R.id.action)
        actionsView.threshold = Int.MAX_VALUE
        actionsView.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, actions))
        actionsView.setOnItemClickListener { _, _, pos, _ ->
            val p = if(defaultCommand) pos else pos+1 // Default command has addition action
            val action = ActionMapper.getActionAt(p)
            viewModel.setSelectedAction(action)
        }
        // Handle Action changed
        viewModel.selectedAction.observe(this) {
            actionsView.setText(ActionMapper.getActionName(this, it))
            // Switch fragment
            actionFragment = ActionMapper.getActionFragment(it)
            showFragment(actionFragment, commandIndex)
        }
    }

    private fun initTitle() {
        val topBar: MaterialToolbar = findViewById(R.id.top_bar)
        when(editMode) {
            DEFAULT_COMMAND -> topBar.title = getString(R.string.edit_default_command)
            UPDATE_COMMAND -> topBar.title = getString(R.string.update_command)
            else -> topBar.title = getString(R.string.add_command)
        }
    }

    private fun initBackButton() {
        val topBar: MaterialToolbar = findViewById(R.id.top_bar)
        topBar.setOnClickListener { goBack() }
    }

    private fun goBack() {
        if(editMode == DEFAULT_COMMAND) {
            startActivity(Intent(this, SettingsActivity::class.java))
        } else {
            startActivity(Intent(this, ListCommandsActivity::class.java))
        }
    }

    private fun initSaveButton() {
        val saveBtn: Button = findViewById(R.id.save)
        saveBtn.setOnClickListener {
            if(viewModel.isValid() and actionFragment.isValid()) {
                viewModel.save(actionFragment.getParameters())
            }
        }
        viewModel.isSaved.observe(this) {
            if(it == true) {
                Toast.makeText(this, R.string.confirm_saved, Toast.LENGTH_SHORT).show()
                goBack()
            }
        }
    }

    private fun showFragment(actionFragment: ActionFragment, commandIndex: Int? = null) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        val args = Bundle()
        args.putString(Constants.KEY_EDIT_MODE, editMode.name)
        commandIndex?.let { args.putInt(Constants.KEY_COMMAND_INDEX, it) }
        actionFragment.arguments = args
        ft.replace(R.id.action_frame, actionFragment)
        ft.commit()

    }

    private fun setValidationErrorListener() {
        viewModel.validationError.observe(this) {
            when(it) {
                ValidationError.NO_KEYWORDS ->
                    cmdLayout.error = getString(R.string.error_no_keywords)
                ValidationError.KEYWORD_EXISTS ->
                    cmdLayout.error = getString(R.string.error_keyword_exists)
                ValidationError.EMPTY_KEYWORD ->
                    cmdLayout.error = getString(R.string.error_empty_keyword)
                else -> Unit
            }
        }
    }
}