package ru.valeripaw.kafka.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@ConfigurationProperties(prefix = "banned-words")
public class BannedWordsProperties {

    private Set<String> root;

    public void update(Set<String> roots) {
        if (CollectionUtils.isEmpty(roots)) {
            this.root = Collections.emptySet();
        }

        Set<String> normalized = roots.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        this.root = Set.copyOf(normalized);
    }

}
