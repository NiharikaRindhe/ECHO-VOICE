package fi.leif.android.voicecommands.view.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import fi.leif.android.voicecommands.R
import fi.leif.android.voicecommands.mappers.CommandMapper
import fi.leif.voicecommands.Command

interface CommandsListListener {
    fun updateCommand(position: Int)
    fun deleteCommand(position: Int)
}

class CommandsListAdapter(private val activity: Activity,
                          private var data: List<Command>,
                          private var listener: CommandsListListener
) : BaseAdapter() {

    fun setData(newData: List<Command>) {
        this.data = newData
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Command {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = activity.layoutInflater
        val rowView = inflater.inflate(R.layout.commands_row, null, true)

        val command: Command = data[position]

        val commandTextView: TextView = rowView.findViewById(R.id.command)
        val actionTextView: TextView = rowView.findViewById(R.id.action_text)
        val deleteBtn: ImageButton = rowView.findViewById(R.id.delete_btn)
        val commandRow: RelativeLayout = rowView.findViewById(R.id.command_row)

        if(position % 2 == 1) {
            commandRow.background = AppCompatResources.getDrawable(activity, R.drawable.gradient)
        }

        val keywords = command.wordsList.joinToString {  it }
        commandTextView.text = keywords
        commandTextView.setTypeface(null, Typeface.BOLD)

        actionTextView.text = CommandMapper.getCommandDescription(activity, command)

        commandRow.setOnClickListener{
            listener.updateCommand(position)
        }
        deleteBtn.setOnClickListener {
            listener.deleteCommand(position)
        }
        return rowView
    }
}