# Chat Manager Bot

## Description

**Chat Manager Bot** is a Telegram bot designed to manage and organize group chats.

- 🚫 Removes all links in messages.
- ✅ Supports whitelist of allowed links.
- 🔑 Allows the use of a secret word to transmit any link (just specify it before the link).
- 🖼 Works with all types of messages: formatted text, photos, audio, video, and more.
- ✏️ Correctly handles situations when a user edits an already sent message.
- 👤 Always shows who sent the message; the sender is indicated at the beginning of the message.
- 🔒 Access can be restricted; you can specify who is allowed to use the bot.

## Examples

### 1. Ignoring by the keyword 🔑

If a keyword is found in the text, for example `skip:` and link isn't in whitelist, then the link remains unchanged.

```
Input: Check it out skip:https://some. I like it!

Output: Check it out https://some. I like it!
```

### 2. Blocking forbidden links 🚫

If the link is not on the whitelist, it is crossed out.

```
Input: Check it out https://forbidden. I like it!

Output: Check it out *************. I like it!
```

### 3. Whitelist ✅

Links from the whitelist are saved.

```
Input: Check it out https://allowed.com

Output: Check it out https://allowed.com
```

## 🧱 Stack

- **Java 17**
- **Gradle**
- **Docker**
- **Spring Boot**
- **Telegram Bot**
- *And others*