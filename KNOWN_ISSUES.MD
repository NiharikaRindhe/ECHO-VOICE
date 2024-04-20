# Known issues

## WhatsApp contacts list missing when "Companion device"
- WhatsApp/Telegram/Phone uses ContentResolver to query app specific contact IDs from the local 
storage (SQLite)
- It seems when you add your Headset Unit WhatsApp account as a "Companion device", the contacts
will not be stored locally, at least they can't be queried using ContentResolver
- Workaround is to **register your account** in the Headset Unit WhatsApp, instead of linking it 
as a Companion device. This adds all the WhatsApp Contacts to the local Content storage
- After registering the Head Unit WhatsApp account, you can go back and change it to a 
Companion device again

