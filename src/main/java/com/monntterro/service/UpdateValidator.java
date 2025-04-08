package com.monntterro.service;

import com.monntterro.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UpdateValidator {
    private final TelegramBot bot;

    @Value("#{'${telegram.chats.white-list}'.split(',\\s+')}")
    private Set<Long> chatsWhiteList;

    public boolean validate(Update update) {
        if (isAvailableChat(update)) {
            return true;
        }

        if ("private".equals(update.getMessage().getChat().getType())) {
            bot.sendMessage("У вас нет доступа к этому боту", update.getMessage().getChatId());
        }
        return false;
    }

    private boolean isAvailableChat(Update update) {
        return chatsWhiteList.contains(update.getMessage().getChat().getId());
    }
}
