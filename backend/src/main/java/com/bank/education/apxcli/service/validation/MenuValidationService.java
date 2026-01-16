package com.bank.education.apxcli.service.validation;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class MenuValidationService {

    private static final Set<String> VALID_TYPES_INIT = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("du-online", "du-lib", "dto", "lib", "trx"))
    );
    private static final Set<String> VALID_TYPES_ADD = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("dto", "lib", "trx"))
    );
    private static final Set<String> NOT_IMPLEMENTED = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("6", "7", "8", "util", "job", "du-batch"))
    );

    private static final Map<String, String> INIT_MENU_MAP;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("1", "du-online");
        map.put("2", "du-lib");
        map.put("3", "dto");
        map.put("4", "lib");
        map.put("5", "trx");
        INIT_MENU_MAP = Collections.unmodifiableMap(map);
    }

    public boolean isNotImplemented(String input) {
        return NOT_IMPLEMENTED.contains(input);
    }

    public boolean isValidType(String input, Set<String> validTypes) {
        return validTypes.contains(input);
    }

    public boolean isValidNumber(String input, Integer max) {
        try {
            int n = Integer.parseInt(input);
            return n >= 1 && n <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isValidInitSelection(String input, Integer max) {
        return isValidNumber(input, max) || isValidType(input, VALID_TYPES_INIT);
    }

    public boolean isValidAddComponentSelection(String input, Integer max) {
        return isValidNumber(input, max) || isValidType(input, VALID_TYPES_ADD);
    }

    public String getTypeForSelection(String input) {
        return INIT_MENU_MAP.getOrDefault(input, input);
    }

    public String getAddComponentTypeForSelection(String input) {
        int n = Integer.parseInt(input) + 2;
        return INIT_MENU_MAP.getOrDefault(String.valueOf(n), input);
    }
}