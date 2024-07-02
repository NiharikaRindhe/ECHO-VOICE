package fi.leif.android.voicecommands.view.fragments.actions

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WhatsAppFragment: RtcFragment() {
    override suspend fun fetchValues() {
        viewModel.fetchWhatsAppContacts()
    }
}