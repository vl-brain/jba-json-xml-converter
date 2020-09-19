package converter.json;

import converter.json.builder.ArrayBuilder;
import converter.json.builder.ObjectBuilder;
import converter.json.builder.PropertyBuilder;

import java.util.ArrayDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonParser {
    private static final Pattern NULL_PATTERN = Pattern.compile("null");
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("(?:true|false)");
    private static final Pattern NUMBER_PATTERN = Pattern.compile(
            "-?(?:0|[1-9]\\d*)(?:\\.\\d+(?:[Ee][+-]\\d+)?)?");
    private static final Pattern STRING_PATTERN = Pattern.compile(
            "\"(?<value>(?:[^\"\\\\]|\\\\(?:[\"\\\\/bfnrt]|u[0-9a-fA-F]{4}))*)\"");
    private static final Pattern PROPERTY_NAME_PATTERN = Pattern.compile(
            "\\s*\"(?<name>[@#]?(?:[^\"\\\\]|\\\\(?:[\"\\\\/bfnrt]|u[0-9a-fA-F]{4}))*)\"\\s*:\\s*");
    private static final Pattern NEXT_ITEM_PATTERN = Pattern.compile("\\s*,\\s*");
    private static final Pattern OPEN_ARRAY_PATTERN = Pattern.compile("\\s*\\[\\s*");
    private static final Pattern CLOSE_ARRAY_PATTERN = Pattern.compile("\\s*]\\s*");
    private static final Pattern OPEN_OBJECT_PATTERN = Pattern.compile("\\s*\\{\\s*");
    private static final Pattern CLOSE_OBJECT_PATTERN = Pattern.compile("\\s*}\\s*");
    private final Matcher openObjectMatcher = OPEN_OBJECT_PATTERN.matcher("");
    private final Matcher closeObjectMatcher = CLOSE_OBJECT_PATTERN.matcher("");
    private final Matcher openArrayMatcher = OPEN_ARRAY_PATTERN.matcher("");
    private final Matcher closeArrayMatcher = CLOSE_ARRAY_PATTERN.matcher("");
    private final Matcher propertyNameMatcher = PROPERTY_NAME_PATTERN.matcher("");
    private final Matcher nextItemMatcher = NEXT_ITEM_PATTERN.matcher("");
    private final Matcher nullMatcher = NULL_PATTERN.matcher("");
    private final Matcher stringMatcher = STRING_PATTERN.matcher("");
    private final Matcher booleanMatcher = BOOLEAN_PATTERN.matcher("");
    private final Matcher numberMatcher = NUMBER_PATTERN.matcher("");
    private final Matcher[] jsonMatchers = {
            openObjectMatcher,
            closeObjectMatcher,
            openArrayMatcher,
            closeArrayMatcher,
            propertyNameMatcher,
            nextItemMatcher,
            nullMatcher,
            stringMatcher,
            booleanMatcher,
            numberMatcher,
    };

    public Json parse(String source) {
        resetMatchers(source);
        final ArrayDeque<ObjectBuilder> stack = new ArrayDeque<>();
        Matcher matcher;
        do {
            matcher = getMatchedMatcher(stack.peekLast());
            if (matcher == openObjectMatcher) {
                stack.add(new PropertyBuilder());
            } else if (matcher == openArrayMatcher) {
                stack.add(new ArrayBuilder());
            } else if (matcher == closeObjectMatcher) {
                @SuppressWarnings("unchecked") final PropertyBuilder propertyBuilder = (PropertyBuilder) stack.removeLast();
                final Json object = new Json(propertyBuilder.build());
                if (stack.isEmpty()) {
                    return object;
                } else {
                    stack.getLast().setValue(object);
                }
            } else if (matcher == closeArrayMatcher) {
                final ArrayBuilder arrayBuilder = (ArrayBuilder) stack.removeLast();
                stack.getLast().setValue(arrayBuilder.build());
            } else if (matcher == propertyNameMatcher) {
                stack.getLast().setProperty(matcher.group("name"));
            } else if (matcher == stringMatcher) {
                stack.getLast().setValue(matcher.group("value"));
            } else if (matcher == booleanMatcher) {
                stack.getLast().setValue(Boolean.valueOf(matcher.group()));
            } else if (matcher == numberMatcher) {
                final Number number = getNumber(matcher.group());
                stack.getLast().setValue(number);
            } else if (matcher == nullMatcher) {
                stack.getLast().setValue(null);
            }
            if (matcher != null) {
                moveMatchersRegion(matcher.end(), source.length());
            }
        } while (matcher != null);
        throw new IllegalArgumentException("Bad format!");
    }

    private Number getNumber(String numValue) {
        if (numValue.indexOf('.') < 0) {
            return Long.parseLong(numValue);
        }
        return Double.parseDouble(numValue);
    }

    public static boolean isJson(String input) {
        return OPEN_OBJECT_PATTERN.matcher(input).lookingAt();
    }

    private Matcher getMatchedMatcher(ObjectBuilder lastBuilder) {
        if (lastBuilder == null) {
            if (openObjectMatcher.lookingAt()) {
                return openObjectMatcher;
            }
        } else {
            final boolean pendingNextProperty = lastBuilder.isPendingNextProperty();
            final boolean pendingNextValue = lastBuilder.isPendingNextValue();
            boolean hasNextItem = (pendingNextProperty || pendingNextValue) && nextItemMatcher.lookingAt();
            if (hasNextItem) {
                moveMatchersRegion(nextItemMatcher.end(), nextItemMatcher.regionEnd());
            }
            final boolean pendingFirstProperty = lastBuilder.isPendingFirstProperty();
            if ((pendingFirstProperty || (pendingNextProperty && hasNextItem)) &&
                    propertyNameMatcher.lookingAt()) {
                return propertyNameMatcher;
            } else if ((pendingFirstProperty ||
                    (pendingNextProperty && !hasNextItem)) &&
                    closeObjectMatcher.lookingAt()) {
                return closeObjectMatcher;
            } else {
                final boolean pendingFirstValue = lastBuilder.isPendingFirstValue();
                if ((pendingFirstValue || (pendingNextValue && !hasNextItem)) && closeArrayMatcher.lookingAt()) {
                    return closeArrayMatcher;
                } else if (pendingFirstValue || (pendingNextValue && hasNextItem)) {
                    return getMatchedValueMatcher();
                }
            }
        }
        return null;
    }

    private Matcher getMatchedValueMatcher() {
        if (nullMatcher.lookingAt()) {
            return nullMatcher;
        } else if (stringMatcher.lookingAt()) {
            return stringMatcher;
        } else if (booleanMatcher.lookingAt()) {
            return booleanMatcher;
        } else if (numberMatcher.lookingAt()) {
            return numberMatcher;
        } else if (openArrayMatcher.lookingAt()) {
            return openArrayMatcher;
        } else if (openObjectMatcher.lookingAt()) {
            return openObjectMatcher;
        }
        return null;
    }

    private void resetMatchers(String source) {
        for (Matcher matcher : jsonMatchers) {
            matcher.reset(source);
        }
    }

    private void moveMatchersRegion(int start, int end) {
        for (Matcher matcher : jsonMatchers) {
            matcher.region(start, end);
        }
    }
}
