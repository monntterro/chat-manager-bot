# Chat Manager Bot

## Description

**Chat Manager Bot** is a Telegram bot designed to manage and organize group chats.

- 🚫 Deletes all links in messages
- ✅ Supports **whitelist** of allowed links
- 🔑 Allows you to use the **secret word** to transmit any link (it is enough to specify it before the link)

The bot works with all types of messages: formatted text, photos, audio and video.  
It also correctly handles situations when a user tries to **change an already sent message**.

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