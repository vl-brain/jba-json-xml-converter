package converter.xml;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlParser {
    private static final Pattern ATTR_SINGLE_QUOTE_PATTERN = Pattern.compile(
            "\\s+(?<name>\\w+)\\s*=\\s*\"(?<value>[^\"]*)\"");
    private static final Pattern ATTR_DOUBLE_QUOTE_PATTERN = Pattern.compile(
            "\\s+(?<name>\\w+)\\s*=\\s*'(?<value>[^']*)'");
    private static final Pattern ATTRIBUTES_PATTERN = Pattern.compile(
            "(?:\\s+\\w+\\s*=\\s*(?:\"[^\"]*\"|'[^']*'))*");
    private static final Pattern PROLOG_PATTERN = Pattern.compile(
            "<\\?xml" + ATTRIBUTES_PATTERN + "\\s*\\?>");
    private static final Pattern EMPTY_ELEMENT_PATTERN = Pattern.compile(
            "\\s*<\\s*(?<tagName>\\w+)(?<attributes>" + ATTRIBUTES_PATTERN + ")\\s*/>");
    private static final Pattern OPEN_TAG_PATTERN = Pattern.compile(
            "\\s*<\\s*(?<tagName>\\w+)(?<attributes>" + ATTRIBUTES_PATTERN + ")\\s*>");
    private static final Pattern VALUE_ELEMENT_PATTERN = Pattern.compile(
            "\\s*<\\s*(?<tagName>\\w+)(?<attributes>" + ATTRIBUTES_PATTERN + ")\\s*>" +
                    "\\s*(?<value>[^<]*)\\s*</\\s*\\k<tagName>\\s*>");
    private static final Pattern CLOSE_TAG_PATTERN = Pattern.compile(
            "\\s*</\\s*(?<tagName>\\w+)\\s*>");
    private final Matcher singleQuoteAttrMatcher = ATTR_SINGLE_QUOTE_PATTERN.matcher("");
    private final Matcher doubleQuoteAttrMatcher = ATTR_DOUBLE_QUOTE_PATTERN.matcher("");
    private final Matcher prologMatcher = PROLOG_PATTERN.matcher("");
    private final Matcher emptyElementMatcher = EMPTY_ELEMENT_PATTERN.matcher("");
    private final Matcher valueElementMatcher = VALUE_ELEMENT_PATTERN.matcher("");
    private final Matcher openTagMatcher = OPEN_TAG_PATTERN.matcher("");
    private final Matcher closeTagMatcher = CLOSE_TAG_PATTERN.matcher("");
    private final Matcher[] tagMatchers = {emptyElementMatcher, valueElementMatcher, openTagMatcher, closeTagMatcher};
    private final Matcher[] attrMatchers = {doubleQuoteAttrMatcher, singleQuoteAttrMatcher};

    public Xml parse(String source) {
        resetMatchers(source);
        skipXmlProlog(source);
        final Deque<XmlNode> stack = new ArrayDeque<>();
        Matcher matcher;
        do {
            matcher = getMatchedMatcher(stack.peekLast());
            if (matcher == valueElementMatcher || matcher == emptyElementMatcher) {
                final XmlNode node = new XmlNode();
                node.setName(matcher.group("tagName"));
                node.setAttributes(parseAttributes(matcher.group("attributes")));
                node.setValue(matcher == valueElementMatcher ? matcher.group("value") : null);
                if (stack.isEmpty()) {
                    return new Xml(node);
                }
                stack.getLast().addChild(node);
            } else if (matcher == openTagMatcher) {
                final XmlNode node = new XmlNode();
                node.setName(matcher.group("tagName"));
                node.setAttributes(parseAttributes(matcher.group("attributes")));
                stack.add(node);
            } else if (matcher == closeTagMatcher) {
                final XmlNode node = stack.pollLast();
                if (stack.isEmpty()) {
                    return new Xml(node);
                } else {
                    stack.getLast().addChild(node);
                }
            }
            if (matcher != null) {
                moveRegion(matcher.end(), matcher.regionEnd(), tagMatchers);
            }
        } while (matcher != null);
        throw new IllegalArgumentException("Bad format!");
    }

    private void skipXmlProlog(String source) {
        if (prologMatcher.reset(source).lookingAt()) {
            final int xmlPrologEnd = prologMatcher.end();
            moveRegion(xmlPrologEnd, prologMatcher.regionEnd(), tagMatchers);
        }
    }

    public static boolean isXml(String source) {
        return PROLOG_PATTERN.matcher(source).lookingAt() ||
                OPEN_TAG_PATTERN.matcher(source).lookingAt();
    }

    private Matcher getMatchedMatcher(XmlNode lastNode) {
        if (emptyElementMatcher.lookingAt()) {
            return emptyElementMatcher;
        } else if (valueElementMatcher.lookingAt()) {
            return valueElementMatcher;
        } else if (openTagMatcher.lookingAt()) {
            return openTagMatcher;
        } else if (lastNode != null &&
                closeTagMatcher.lookingAt() &&
                lastNode.getName().equals(closeTagMatcher.group("tagName"))) {
            return closeTagMatcher;
        }
        return null;
    }

    private void resetMatchers(String input) {
        for (Matcher matcher : tagMatchers) {
            matcher.reset(input);
        }
    }

    private void moveRegion(int start, int end, Matcher[] matchers) {
        for (Matcher matcher : matchers) {
            matcher.region(start, end);
        }
    }

    private Map<String, String> parseAttributes(String value) {
        doubleQuoteAttrMatcher.reset(value);
        singleQuoteAttrMatcher.reset(value);
        Matcher matcher = doubleQuoteAttrMatcher.lookingAt() ? doubleQuoteAttrMatcher :
                (singleQuoteAttrMatcher.lookingAt() ? singleQuoteAttrMatcher : null);
        if (matcher == null) {
            return Map.of();
        }
        final Map<String, String> attributes = new LinkedHashMap<>();
        do {
            attributes.put(matcher.group("name"), matcher.group("value"));
            moveRegion(matcher.end(), matcher.regionEnd(), attrMatchers);
            matcher = doubleQuoteAttrMatcher.lookingAt() ? doubleQuoteAttrMatcher :
                    (singleQuoteAttrMatcher.lookingAt() ? singleQuoteAttrMatcher : null);
        } while (matcher != null);
        return attributes;
    }
}
