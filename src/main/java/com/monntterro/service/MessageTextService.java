package com.monntterro.service;

import com.monntterro.model.ProcessedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageTextService {
    private static final String TEXT_LINK_TYPE = "text_link";
    private static final String URL_TYPE = "url";
    private static final String TEXT_MENTION_TYPE = "text_mention";
    private static final String PRIVATE_CHAT_TYPE = "private";

    private final Pattern urlPattern = Pattern.compile("(?i)(?:(?:(?:https?|ftp)://|www\\.)([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}|\\d{1,3}(?:\\.\\d{1,3}){3}|\\[[a-fA-F0-9:]+])(:\\d{1,5})?(/\\S*)?|(t\\.me/\\S+))");
    private final Pattern mentionPattern = Pattern.compile("@[a-zA-Z0-9_]+");

    private final TelegramBot bot;

    @Value("${telegram.secret-word-to-pass}")
    private String secretWordToPass;

    @Value("#{'${telegram.urls.white-list}'.split(',\\s+')}")
    private List<String> urlsWhiteList;

    public ProcessedMessage deleteLinks(Message message, String text, List<MessageEntity> entities) {
        String username = formatUsername(message);
        String newText = "%s:\n%s".formatted(username, text);

        List<MessageEntity> adjustedEntities = adjustEntities(entities, username);
        adjustedEntities.add(createUserMention(message, username));

        String processedText = processMentions(newText);
        processedText = processUrls(processedText);

        return new ProcessedMessage(processedText, adjustedEntities);
    }

    public boolean hasLinksInMessage(String text, List<MessageEntity> entities) {
        boolean hasPatternMatches = urlPattern.matcher(text).find() || mentionPattern.matcher(text).find();
        boolean hasLinkEntities = entities.stream()
                .map(MessageEntity::getType)
                .anyMatch(type -> TEXT_LINK_TYPE.equals(type) || URL_TYPE.equals(type));

        return hasPatternMatches || hasLinkEntities;
    }

    public boolean hasOnlyAllowedLinks(String text, List<MessageEntity> entities) {
        boolean onlyAllowedUrls = urlPattern.matcher(text).results()
                .map(MatchResult::group)
                .allMatch(this::isUrlInWhitelist);
        boolean onlyAllowedTextLinks = entities.stream()
                .filter(entity -> TEXT_LINK_TYPE.equals(entity.getType()))
                .allMatch(entity -> isUrlInWhitelist(entity.getUrl()));

        return onlyAllowedUrls && onlyAllowedTextLinks && hasOnlyUserMentions(text);
    }

    private String formatUsername(Message message) {
        String lastName = message.getFrom().getLastName();
        return message.getFrom().getFirstName() + (lastName == null ? "" : " " + lastName);
    }

    private List<MessageEntity> adjustEntities(List<MessageEntity> entities, String username) {
        if (entities == null) {
            return new ArrayList<>();
        }
        int offset = 2 + username.length(); // For the format: "username:\n"

        return entities.stream()
                .filter(this::isAllowedEntity)
                .peek(entity -> entity.setOffset(entity.getOffset() + offset))
                .collect(Collectors.toList());
    }

    private boolean isAllowedEntity(MessageEntity entity) {
        if (TEXT_LINK_TYPE.equals(entity.getType())) {
            return isUrlInWhitelist(entity.getUrl());
        }
        return true;
    }

    private boolean isUrlInWhitelist(String url) {
        return urlsWhiteList.stream().anyMatch(url::startsWith);
    }

    private MessageEntity createUserMention(Message message, String username) {
        return MessageEntity.builder()
                .user(message.getFrom())
                .type(TEXT_MENTION_TYPE)
                .user(message.getFrom())
                .offset(0)
                .length(username.length())
                .build();
    }

    private String processMentions(String text) {
        Matcher matcher = mentionPattern.matcher(text);
        StringBuilder result = new StringBuilder(text);

        while (matcher.find()) {
            String mention = matcher.group();
            int start = matcher.start();

            if (!isAllowedMention(mention, text, start)) {
                int replaceStart = matcher.start();
                int replaceEnd = matcher.end();
                String replacement = "*".repeat(mention.length());

                result.replace(replaceStart, replaceEnd, replacement);
            }
        }

        return result.toString();
    }

    private boolean isAllowedMention(String mention, String text, int mentionStart) {
        try {
            Chat chat = bot.getChat(mention);
            if (!PRIVATE_CHAT_TYPE.equals(chat.getType())) {
                return hasPrecedingSecretWord(text, mentionStart);
            }
            return true;
        } catch (TelegramApiException ignored) {
            return true;
        }
    }

    private boolean hasPrecedingSecretWord(String text, int position) {
        if (position > secretWordToPass.length()) {
            String substring = text.substring(position - secretWordToPass.length(), position);
            return secretWordToPass.equals(substring);
        }
        return false;
    }

    private String processUrls(String text) {
        Matcher matcher = urlPattern.matcher(text);
        StringBuilder result = new StringBuilder(text);
        while (matcher.find()) {
            String url = matcher.group();
            int start = matcher.start();
            if (hasPrecedingSecretWord(text, start)) {
                result.replace(start - secretWordToPass.length(), start, " ".repeat(secretWordToPass.length()));
            } else if (!isUrlInWhitelist(url)) {
                result.replace(matcher.start(), matcher.end(), "*".repeat(url.length()));
            }
        }

        return result.toString();
    }

    private boolean hasOnlyUserMentions(String text) {
        Matcher matcher = mentionPattern.matcher(text);
        while (matcher.find()) {
            String mention = matcher.group();
            try {
                Chat chat = bot.getChat(mention);
                if (!PRIVATE_CHAT_TYPE.equals(chat.getType())) {
                    return false;
                }
            } catch (TelegramApiException ignored) {
                return true;
            }
        }
        return true;
    }
}