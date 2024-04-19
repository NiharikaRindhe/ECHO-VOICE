package fi.leif.android.voicecommands.viewmodel

class Constants {
    companion object {
        const val KEY_COMMAND_INDEX = "commandIndex"
        const val KEY_EDIT_MODE = "editMode"
    }
}

enum class ValidationError {
    EMPTY_KEYWORD,
    NO_KEYWORDS,
    KEYWORD_EXISTS,
    INVALID_RMS_DB
}

enum class EditMode {
    NEW_COMMAND,
    UPDATE_COMMAND,
    DEFAULT_COMMAND
}
