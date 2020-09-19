package converter.json;

import converter.intermediate.Intermediate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonConverter {
    private JsonConverter() {
    }

    public static Json convert(Intermediate other) {
        return new Json(Map.of(other.getName(), getValue(other)));
    }

    private static Object getValue(Intermediate source) {
        final List<Intermediate> children = source.getChildren();
        final Map<String, String> attributes = source.getAttributes();
        Object value = source.getValue();
        if (!children.isEmpty()) {
            if (children.size() > 1 && children.get(0).getName().equals(children.get(1).getName())) {
                final List<Object> list = new ArrayList<>();
                for (Intermediate child : children) {
                    list.add(getValue(child));
                }
                value = list;
            } else {
                final Map<String, Object> props = new LinkedHashMap<>(children.size());
                for (Intermediate child : children) {
                    props.put(child.getName(), getValue(child));
                }
                value = new Json(props);
            }
        }
        if (!attributes.isEmpty()) {
            final Map<String, Object> props = new LinkedHashMap<>(attributes.size() + 1);
            attributes.forEach((attrName, attrValue) -> props.put("@" + attrName, attrValue));
            props.put("#" + source.getName(), value);
            value = new Json(props);
        }
        return value;
    }

}
