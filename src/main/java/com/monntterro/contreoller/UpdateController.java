package com.monntterro.contreoller;

import com.monntterro.handler.UpdateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@RequiredArgsConstructor
@RestController
public class UpdateController {
    private final UpdateHandler updateHandler;

    @PostMapping("/callback/${telegram.bot.path}")
    public ResponseEntity<?> handleUpdate(@RequestBody Update update) {
        try {
            updateHandler.handle(update);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok().build();
    }
}
