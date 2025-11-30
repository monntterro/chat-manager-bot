package com.monntterro.command;

import com.monntterro.service.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetChatId implements Command {
    public static final String COMMAND = "/chat_id";

    private final TelegramBot bot;

    private static MessageEntity generateCodeBlock(String text) {
        return MessageEntity.builder()
                .type("code")
                .offset(0)
                .length(text.length())
                .build();
    }

    @Override
    public void execute(Message message) {
        String text = message.getChatId().toString();
        List<MessageEntity> entities = List.of(generateCodeBlock(text));
        bot.sendReplyMessage(text, message.getChatId(), message.getMessageId(), entities);
    }

    @Override
    public String getCommand() {
        return COMMAND;
    }

    @Override
    public boolean isPriority() {
        return false;
    }
}
