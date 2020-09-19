package converter.intermediate;

import converter.json.Json;
import converter.xml.Xml;
import converter.xml.XmlNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IntermediateConverter {

    public static Intermediate convert(Xml xml) {
        return convert(xml.getRoot());
    }

    private static Intermediate convert(XmlNode other) {
        final Intermediate node = new Intermediate(other.getName(), other.getAttributes(), other.getValue());
        for (XmlNode child : other.getChildren()) {
            node.addChild(convert(child));
        }
        return node;
    }

    public static Intermediate convert(Json json) {
        Intermediate root = new Intermediate("root");
        parse(json, root);
        if (root.getChildren().size() == 1) {
            root = root.getChildren().get(0);
            root.setParent(null);
        }
        return root;
    }

    private static void parse(Json json, Intermediate parent) {
        json.getProperties().forEach((name, value) ->
                parse(name, value, parent));
    }

    private static void parse(String name, Object value, Intermediate parent) {
        name = name.replaceFirst("^[@#]", "");
        if (name.isEmpty()) {
            return;
        }
        final Intermediate intermediate = new Intermediate(name);
        parent.addChild(intermediate);
        if (value instanceof Json) {
            final Map<String, Object> props = ((Json) value).getProperties();
            final boolean hasRemovedProps = cleanProps(props);
            if (!hasRemovedProps && hasValueAndAttributes(name, props)) {
                intermediate.setAttributes(parseAttributes(props));
                value = props.get("#" + name);
            }
        }
        if (Json.isSimpleType(value)) {
            intermediate.setValue(value == null ? null : String.valueOf(value));
        } else if (Json.isEmptyObject(value)) {
            intermediate.setValue("");
        } else if (value instanceof List) {
            final List<?> list = (List<?>) value;
            for (Object item : list) {
                parse("element", item, intermediate);
            }
        } else if (value instanceof Json) {
            parse((Json) value, intermediate);
        }
    }

    private static boolean cleanProps(Map<String, Object> properties) {
        return properties.keySet().removeIf(property -> property.isEmpty() ||
                "@".equals(property) || "#".equals(property) ||
                ((property.charAt(0) == '@' || property.charAt(0) == '#') &&
                        properties.containsKey(property.substring(1))));
    }

    private static boolean hasValueAndAttributes(String name, Map<String, Object> value) {
        boolean hasValue = false;
        for (Map.Entry<String, Object> entry : value.entrySet()) {
            final String attr = entry.getKey();
            final Object attrVal = entry.getValue();
            if (attr.isEmpty()) {
                return false;
            }
            final char startChar = attr.charAt(0);
            if (startChar == '@' && attr.length() > 1 &&
                    (Json.isSimpleType(attrVal) || Json.isEmptyObject(attrVal))) {
                continue;
            }
            if (startChar == '#' && name.equals(attr.substring(1))) {
                hasValue = true;
                continue;
            }
            return false;
        }
        return hasValue;
    }

    private static Map<String, String> parseAttributes(Map<String, Object> properties) {
        return properties.keySet().stream()
                .filter(key -> key.startsWith("@"))
                .collect(Collectors.toMap(key -> key.substring(1),
                        key -> {
                            final Object attrValue = properties.get(key);
                            return attrValue != null && Json.isSimpleType(attrValue) ? String.valueOf(attrValue) : "";
                        },
                        (a, b) -> b,
                        LinkedHashMap::new));
    }
}
