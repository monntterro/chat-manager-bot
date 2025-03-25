package com.monntterro.service;

import com.monntterro.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageTextService {
    private final Pattern urlPattern = Pattern.compile("(?i)(?:(?:(?:https?|ftp)://|www\\.)([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|\\d{1,3}(?:\\.\\d{1,3}){3}|\\[[a-fA-F0-9:]+])(:\\d{1,5})?(/\\S*)?|(t\\.me/\\S+))");
    private final Pattern mentionPattern = Pattern.compile("@[a-zA-Z0-9_]+");

    private final TelegramBot bot;

    @Value("${telegram.secret-word-to-pass}")
    private String secretWordToPass;
    @Value("#{'${telegram.urls.white-list}'.split(',\\s+')}")
    private List<String> urlsWhiteList;

    public String deleteLinks(Message message, String text, List<MessageEntity> entities) {
        String lastName = message.getFrom().getLastName();
        String username = message.getFrom().getFirstName() + (lastName == null ? "" : " " + lastName);
        text = "%s:\n%s".formatted(username, text);

        List<MessageEntity> filteredEntities = entities.stream()
                .filter(entity -> {
                    if ("text_link".equals(entity.getType())) {
                        return urlsWhiteList.stream().anyMatch(entity.getUrl()::startsWith);
                    }
                    return true;
                })
                .peek(entity -> entity.setOffset(entity.getOffset() + 2 + username.length()))
                .toList();
        entities.clear();
        entities.addAll(filteredEntities);
        entities.add(userMention(message, username));

        Matcher matcher = mentionPattern.matcher(text);
        while (matcher.find()) {
            String mention = matcher.group();
            GetChat getChat = new GetChat(mention);
            try {
                Chat chat = bot.execute(getChat);
                if (chat.getType().equals("private")) {
                    continue;
                }
                int start = matcher.start();
                if (start > secretWordToPass.length()) {
                    String substring = text.substring(start - secretWordToPass.length(), start);
                    if (secretWordToPass.equals(substring)) {
                        text = text.replaceFirst(Pattern.quote(secretWordToPass + mention), " ".repeat(secretWordToPass.length()) + mention);
                        continue;
                    }
                }
                text = text.replace(mention, "*".repeat(mention.length()));
            } catch (TelegramApiException ignored) {
            }
        }

        matcher = urlPattern.matcher(text);
        while (matcher.find()) {
            String url = matcher.group();
            if (urlsWhiteList.stream().anyMatch(url::startsWith)) {
                continue;
            }
            int start = matcher.start();
            if (start > secretWordToPass.length()) {
                String substring = text.substring(start - secretWordToPass.length(), start);
                if (secretWordToPass.equals(substring)) {
                    text = text.replaceFirst(Pattern.quote(secretWordToPass + url), " ".repeat(secretWordToPass.length()) + url);
                    continue;
                }
            }
            text = text.replace(url, "*".repeat(url.length()));
        }
        return text;
    }

    public boolean hasLinksInMessage(String text, List<MessageEntity> entities) {
        if (urlPattern.matcher(text).find() || mentionPattern.matcher(text).find()) {
            return true;
        }
        return entities.stream()
                .map(MessageEntity::getType)
                .anyMatch(type -> "text_link".equals(type) || "url".equals(type));
    }

    private MessageEntity userMention(Message message, String username) {
        return MessageEntity.builder()
                .user(message.getFrom())
                .type("text_mention")
                .user(message.getFrom())
                .offset(0)
                .length(username.length())
                .build();
    }

    public boolean hasOnlyAllowedLinks(String text, List<MessageEntity> entities) {
        boolean onlyAllowedUrls = urlPattern.matcher(text).results()
                .map(MatchResult::group)
                .allMatch(url -> urlsWhiteList.stream().anyMatch(url::startsWith));
        boolean onlyAllowedTextLinks = entities.stream()
                .filter(entity -> "text_link".equals(entity.getType()))
                .allMatch(entity -> urlsWhiteList.stream().anyMatch(entity.getUrl()::startsWith));
        return onlyAllowedUrls && onlyAllowedTextLinks && hasOnlyUserMentions(text);
    }

    private boolean hasOnlyUserMentions(String text) {
        Matcher matcher = mentionPattern.matcher(text);
        while (matcher.find()) {
            String mention = matcher.group();
            GetChat getChat = new GetChat(mention);
            try {
                Chat chat = bot.execute(getChat);
                if (!chat.getType().equals("private")) {
                    return false;
                }
            } catch (TelegramApiException ignored) {
            }
        }
        return true;
    }
}
