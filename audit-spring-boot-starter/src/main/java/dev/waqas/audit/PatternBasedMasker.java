package dev.waqas.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import dev.waqas.audit.autoconfigure.AuditProperties;

/**
 * Applies configured regex replacements to redact sensitive substrings (e.g. card numbers).
 */
@Component
public class PatternBasedMasker {

    private final List<Pattern> patterns;

    public PatternBasedMasker(AuditProperties properties) {
        this.patterns = new ArrayList<>();
        for (String p : properties.getMasking().getPatterns()) {
            if (p != null && !p.isBlank()) {
                patterns.add(Pattern.compile(p));
            }
        }
    }

    public String mask(String value) {
        if (value == null || value.isEmpty() || patterns.isEmpty()) {
            return value;
        }
        String out = value;
        for (Pattern pattern : patterns) {
            out = pattern.matcher(out).replaceAll("***");
        }
        return out;
    }
}
