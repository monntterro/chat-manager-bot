package com.monntterro.processor;

import com.monntterro.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandProcessor {
    private final TelegramBot bot;

    private static MessageEntity generateCodeBlock(String text) {
        return MessageEntity.builder()
                .type("code")
                .offset(0)
                .length(text.length())
                .build();
    }

    public void process(Message message) {
        String command = message.getText();

        switch (command) {
            case "/chat_id" -> chatIdCommand(message);
        }
    }

    private void chatIdCommand(Message message) {
        String text = message.getChatId().toString();
        List<MessageEntity> entities = List.of(generateCodeBlock(text));
        bot.sendReplyMessage(text, message.getChatId(), message.getMessageId(), entities);
    }
}
