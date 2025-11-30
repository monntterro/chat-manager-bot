package com.monntterro.model;

import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.util.List;

public record ProcessedMessage(String text, List<MessageEntity> entities) {}
