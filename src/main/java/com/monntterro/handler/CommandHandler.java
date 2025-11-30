package com.monntterro.handler;

import com.monntterro.command.Command;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommandHandler {
    private final Map<String, Command> commands;

    public CommandHandler(List<Command> commands) {
        this.commands = commands.stream()
                .collect(Collectors.toMap(Command::getCommand, Function.identity()));
    }

    public void handle(Message message) {
        if (!message.hasText()) {
            return;
        }

        String commandName = message.getText().split("\\s+", 2)[0];
        Command command = commands.get(commandName);
        if (command != null) {
            command.execute(message);
        } else {
            log.warn("Command not found: {}", commandName);
        }
    }
}
