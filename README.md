# Chat Manager Bot

## Description

**Chat Manager Bot** is a Telegram bot designed to manage and organize group chats with advanced link filtering capabilities.

- 🚫 Removes all links in messages.
- ✅ Supports whitelist of allowed links.
- 🔑 Allows the use of a secret word to transmit any link (just specify it before the link).
- 🖼 Works with all types of messages: formatted text, photos, audio, video, documents, stickers, animations, voice messages, and more.
- ✏️ Correctly handles situations when a user edits an already sent message.
- 👤 Always shows who sent the message; the sender is indicated at the beginning of the message.
- 🔒 Access can be restricted; you can specify who is allowed to use the bot.
- 📊 Built-in metrics monitoring with Prometheus support.

## Features

### Link Management
- **URL Detection**: Automatically detects URLs in messages using regex patterns
- **Whitelist Support**: Configure allowed domains that won't be filtered
- **Secret Word Bypass**: Use a secret keyword before links to temporarily allow them
- **Text Link Filtering**: Handles both plain URLs and Telegram text links
- **Mention Control**: Manages @mentions and prevents unwanted channel/group mentions

### Media Support
- **Album Handling**: Properly processes media groups (multiple photos/videos sent together)
- **Multiple Formats**: Photos, videos, documents, audio, animations, stickers, video notes, voice messages
- **Caption Processing**: Filters links in media captions while preserving formatting

### Security
- **Chat Whitelist**: Restrict bot usage to specific chats only
- **Private Chat Notifications**: Informs unauthorized users when they try to use the bot
- **Message Attribution**: Always shows the original sender to prevent impersonation

## Examples

### 1. Ignoring by the keyword 🔑

If a keyword is found in the text (e.g., `skip:`) and the link isn't in whitelist, then the link remains unchanged.

```
Input: Check it out skip:https://example.com. I like it!

Output: Check it out https://example.com. I like it!
```

### 2. Blocking forbidden links 🚫

If the link is not on the whitelist, it is crossed out.

```
Input: Check it out https://forbidden.com. I like it!

Output: Check it out **********************. I like it!
```

### 3. Whitelist ✅

Links from the whitelist are saved.

```
Input: Check it out https://allowed.com

Output: Check it out https://allowed.com
```

## 🧱 Tech Stack

- **Java 17**
- **Gradle**
- **Docker**
- **Spring Boot**
- **Telegram Bot API**
- **Micrometer & Prometheus** (for metrics)

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Gradle
- Telegram Bot Token (get it from [@BotFather](https://t.me/botfather))

### Configuration

Create a `.env` file or set environment variables:

```env
BOT_TOKEN=your_telegram_bot_token
SECRET_WORD_TO_PASS=skip:
URLS_WHITE_LIST=https://allowed.com, https://example.org
CHAT_IDS_WHITE_LIST=123456789, -987654321
```

**Configuration Parameters:**

- `BOT_TOKEN` - Your Telegram bot token
- `SECRET_WORD_TO_PASS` - Keyword that allows bypassing link filtering
- `URLS_WHITE_LIST` - Comma-separated list of allowed URL prefixes
- `CHAT_IDS_WHITE_LIST` - Comma-separated list of allowed chat IDs

### Running the Application

#### Using Gradle

```bash
./gradlew bootRun
```

#### Using Docker

```bash
docker run -d \
  -e BOT_TOKEN=your_token \
  -e SECRET_WORD_TO_PASS=skip: \
  -e URLS_WHITE_LIST=https://allowed.com \
  -e CHAT_IDS_WHITE_LIST=123456789 \
  monntterro/chat-manager-bot:latest
```

## 📊 Monitoring

The bot exposes Prometheus metrics for monitoring:

- **Endpoint**: `http://localhost:8081/actuator/prometheus`
- **Health Check**: `http://localhost:8081/actuator/health`

### Available Metrics

- `message.processed` - Counter of successfully processed messages
- `message.skipped` - Counter of skipped messages (no links or only allowed links)

## 🤖 Bot Commands

- `/chat_id` - Get the current chat ID (useful for configuration)

## 📝 How It Works

1. **Message Reception**: Bot receives a message from a Telegram chat
2. **Validation**: Checks if the chat is in the whitelist
3. **Link Detection**: Scans message text and entities for URLs and mentions
4. **Processing**:
   - If secret word is present before a link → link is allowed
   - If link is in whitelist → link is preserved
   - Otherwise → link is replaced with asterisks
5. **Reposting**: Original message is deleted and reposted with filtered content
6. **Attribution**: Sender's name is added at the beginning of the message

### Special Cases

- **Media Groups**: Photos/videos sent as albums are processed together with a 3-second delay to ensure all media is collected
- **Edited Messages**: Bot reprocesses edited messages with the same filtering rules
- **Forwarded Messages**: Messages forwarded from channels are always processed
- **Channel/Group Mentions**: @mentions of channels/groups are blocked unless preceded by the secret word

## 💡 Tips

- To find your chat ID, use the `/chat_id` command in your chat
- For group chats, add the bot as an administrator
- Test the bot in a private chat first before deploying to groups
- Monitor metrics regularly to track bot performance
