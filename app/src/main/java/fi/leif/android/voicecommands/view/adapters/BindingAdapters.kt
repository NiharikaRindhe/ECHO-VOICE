package fi.leif.android.voicecommands.view.adapters

import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import fi.leif.android.voicecommands.repositories.App
import fi.leif.android.voicecommands.repositories.Contact

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("items", "lifecycle")
    fun setItems(view: AutoCompleteTextView, items: LiveData<List<String>>, lifecycleOwner: LifecycleOwner) {
        items.observe(lifecycleOwner) { list ->
            val adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, list)
            view.threshold = Int.MAX_VALUE // Show all items
            view.setAdapter(adapter)
        }
    }

    @JvmStatic
    @BindingAdapter("contacts", "lifecycle")
    fun setContacts(view: AutoCompleteTextView, items: LiveData<List<Contact>>, lifecycleOwner: LifecycleOwner) {
        items.observe(lifecycleOwner) {
            // Add a blank first element for the user to select
            val contacts = arrayListOf<Contact?>(Contact("", "", ""))
            items.value?.let { contacts.addAll(it) }
            val adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, contacts)
            view.threshold = Int.MAX_VALUE // Show all items
            view.setAdapter(adapter)
        }
    }

    @JvmStatic
    @BindingAdapter("apps", "lifecycle")
    fun setApps(view: AutoCompleteTextView, apps: LiveData<List<App>>, lifecycleOwner: LifecycleOwner) {
        apps.observe(lifecycleOwner) {
            val adapter = ArrayAdapter(view.context, android.R.layout.simple_list_item_1, it)
            view.threshold = Int.MAX_VALUE // Show all items
            view.setAdapter(adapter)
        }
    }

}