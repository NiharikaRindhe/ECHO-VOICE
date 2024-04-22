package fi.leif.android.voicecommands.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import fi.leif.android.voicecommands.R
import fi.leif.android.voicecommands.view.adapters.CommandsListAdapter
import fi.leif.android.voicecommands.view.adapters.CommandsListListener
import fi.leif.android.voicecommands.viewmodel.Constants
import fi.leif.android.voicecommands.viewmodel.EditMode
import fi.leif.android.voicecommands.viewmodel.ListCommandsViewModel

class ListCommandsActivity : AppCompatActivity(), CommandsListListener {

    private val viewModel: ListCommandsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_commands)

        // Init Commands List View
        val commandsAdapter = CommandsListAdapter(this, ArrayList(), this)
        val commandsListView: ListView = findViewById(R.id.commands)
        commandsListView.adapter = commandsAdapter

        viewModel.commands.observe(this) {
            commandsAdapter.setData(it)
            commandsAdapter.notifyDataSetChanged()
        }

        // Add command button
        val addBtn: Button = findViewById(R.id.add_command)
        addBtn.setOnClickListener{
            val intent = Intent(this, EditCommandActivity::class.java)
            intent.putExtra(Constants.KEY_EDIT_MODE, EditMode.NEW_COMMAND.name)
            startActivity(intent)
        }

        // Command deleted observer
        viewModel.isDeleted.observe(this) {
            if(it == true) {
                Toast.makeText(this, R.string.confirm_deleted, Toast.LENGTH_SHORT).show()
            }
        }

        // Back button
        val topBar: MaterialToolbar = findViewById(R.id.top_bar)
        topBar.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun updateCommand(position: Int) {
        val intent = Intent(this, EditCommandActivity::class.java)
        intent.putExtra(Constants.KEY_EDIT_MODE, EditMode.UPDATE_COMMAND.name)
        intent.putExtra(Constants.KEY_COMMAND_INDEX, position)
        startActivity(intent)

    }

    override fun deleteCommand(position: Int){
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_command_title)
            .setMessage(R.string.delete_command_question)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.deleteCommand(position)
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}