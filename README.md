# Voice Commands
I bought an Android Head Unit (Ainavi H6/AliExpress) for my Toyta Auris, as the factory Toyota Head Unit was very limited.

The Ainavi H6 works fine, but for whatever reason (cost?) it ships with a version of Google Assistant that is not very multilingual, at least not in terms of Speech Recognition. Firstly, Speech Recognition is tied to the System Language (which I'd prefer to be English). Secondly, it lacks Finnish Language Voice Recognition support (I live in Finland). It therefore gets tricky to Voice Control Navigation to a Finnish address.

As a workaround I coded a custom Android App for Voice Controlled Commands, which uses Android SpeechRecognizer (https://developer.android.com/reference/android/speech/SpeechRecognizer). It supports Voice Recognition with a configurable Extra Language in addition to the System Language.

## Demo Video
[![Demo video](https://img.youtube.com/vi/6ozP9WjGnLo/0.jpg)](https://www.youtube.com/watch?v=6ozP9WjGnLo)

## Known Issues
The App works well when the background noise level is low, however speech recognition is **not very good** when there's loads of background noise (fan/Air Conditioner, driving with high speed, etc).

