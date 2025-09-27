# Chat Manager Bot

## Description

**Chat Manager Bot** is a Telegram bot designed to help manage and organize group chats. It removes all links in
messages, but it also has a whitelist of links. It also allows you to create a word that allows you to transmit
any link, just put a secret word in front of the link.

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



