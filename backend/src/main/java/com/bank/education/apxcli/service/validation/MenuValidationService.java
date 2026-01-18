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
            new HashSet<>(Arrays.asList("du-online", "du-lib", "dto", "lib", "trx", "library", "transaction")));
    private static final Set<String> VALID_TYPES_ADD = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("dto", "lib", "trx","library", "transaction")));
    private static final Set<String> NOT_IMPLEMENTED = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("6", "7", "8", "util", "job", "du-batch")));

    private static final Map<String, String> INIT_MENU_MAP;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("1", "du-online");
        map.put("2", "du-lib");
        map.put("3", "dto");
        map.put("4", "lib");
        map.put("5", "trx");
        map.put("library", "lib");
        map.put("transaction", "trx");
        INIT_MENU_MAP = Collections.unmodifiableMap(map);
    }

    private static final Map<String, String> ADD_MENU_MAP;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("1", "dto");
        map.put("2", "lib");
        map.put("3", "trx");
        map.put("library", "lib");
        map.put("transaction", "trx");
        ADD_MENU_MAP = Collections.unmodifiableMap(map);
    }

    private static final Map<String, String> ADD_DEP_MENU_MAP;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("1", "dto");
        map.put("2", "lib");
        map.put("library", "lib");
        ADD_DEP_MENU_MAP = Collections.unmodifiableMap(map);
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

    public boolean isValidAddDepSelection(String input, String sourceType) {
        if (sourceType.equals("dto") || sourceType.equals("lib")) {
            return (isValidNumber(input, 1) || input.equals("dto"));
        } else if (sourceType.equals("lib-impl") || sourceType.equals("trx")) {
            return (isValidNumber(input, 2) || input.equals("dto")
                    || input.equals("lib") || input.equals("library"));
        } else {
            return false;
        }
    }

    public String getTypeForSelection(String input) {
        return INIT_MENU_MAP.getOrDefault(input, input);
    }

    public String getAddComponentTypeForSelection(String input) {
        return ADD_MENU_MAP.getOrDefault(input, input);
    }

    public String getAddDepSourceTypeForSelection(String input) {
        return ADD_DEP_MENU_MAP.getOrDefault(input, input);
    }
}