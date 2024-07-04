package fi.leif.android.voicecommands.view.fragments.actions

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TelegramFragment: RtcFragment() {
    override suspend fun fetchValues() {
        super.fetchValues()
        viewModel.fetchTelegramContacts()
    }
}