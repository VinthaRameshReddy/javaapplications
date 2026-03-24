package com.medgo.patternlayout;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MaskingPatternLayout extends PatternLayout {

    private Pattern multilinePattern;
    private final List<String> maskPatterns = new ArrayList<>();

    public void addMaskPattern(String maskPattern) {
        maskPatterns.add(maskPattern);
        multilinePattern = Pattern.compile(maskPatterns.stream().collect(Collectors.joining("|")), Pattern.MULTILINE);
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        return maskMessage(super.doLayout(event));
    }

    private String maskMessage(String message) {
        if (multilinePattern == null) {
            return message;
        }

        StringBuffer sb = new StringBuffer();
        Matcher matcher = multilinePattern.matcher(message);
        while (matcher.find()) {
            for (int group = 1; group <= matcher.groupCount(); group++) {
                if (matcher.group(group) != null) {
                    String original = matcher.group(group);
                    String masked = maskValue(original);
                    matcher.appendReplacement(sb, matcher.group().replace(original, masked));
                }
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String maskValue(String value) {
        if (value == null || value.length() <= 4) {
            return "****";
        }
        return "****" + value.substring(4); // mask first 4 characters
    }
}
