package com.monntterro.command;

import org.telegram.telegrambots.meta.api.objects.message.Message;

public interface Command {

    void execute(Message message);

    String getCommand();

    boolean isPriority();
}
