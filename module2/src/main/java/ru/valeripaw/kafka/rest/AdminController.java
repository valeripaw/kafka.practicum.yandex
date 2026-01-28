package ru.valeripaw.kafka.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.valeripaw.kafka.properties.BannedWordsProperties;

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final BannedWordsProperties bannedWordsProperties;

    @PostMapping("/admin/banned-words")
    public void updateBannedWords(@RequestBody Set<String> roots) {
        bannedWordsProperties.update(roots);
    }

}
