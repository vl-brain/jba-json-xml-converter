package converter.json;

import java.util.List;
import java.util.Map;

public class Json {
    private final Map<String, Object> properties;

    public Json(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public static boolean isSimpleType(Object jsonValue) {
        return jsonValue == null ||
                jsonValue instanceof String ||
                jsonValue instanceof Number ||
                jsonValue instanceof Boolean;
    }

    public static boolean isEmptyObject(Object jsonValue) {
        return (jsonValue instanceof Json && ((Json) jsonValue).getProperties().isEmpty()) ||
                (jsonValue instanceof List && ((List<?>) jsonValue).isEmpty());
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        addJsonToBuilder(builder, 0);
        return builder.toString();
    }

    protected void addJsonToBuilder(StringBuilder builder, int rootPathOffset) {
        final String indent = " ".repeat(4 * rootPathOffset);
        if (properties.isEmpty()) {
            builder.append("{}");
        } else {
            builder.append("{\n");
            final String propIndent = " ".repeat(4 * (rootPathOffset + 1));
            properties.forEach((name, value) -> {
                builder.append(propIndent)
                        .append("\"")
                        .append(name)
                        .append("\": ");
                addJsonValue(builder, value, rootPathOffset + 1);
                builder.append(",\n");
            });
            builder.delete(builder.length() - 2, builder.length() - 1);
            builder.append(indent).append("}");
        }
    }

    private void addJsonValue(StringBuilder builder, Object value, int rootPathOffset) {
        if (value == null ||
                value instanceof Boolean ||
                value instanceof Number) {
            builder.append(value);
        } else if (value instanceof String) {
            builder.append('"')
                    .append(value)
                    .append('"');
        } else if (value instanceof Json) {
            ((Json) value).addJsonToBuilder(builder, rootPathOffset);
        } else if (value instanceof List) {
            final List<?> list = (List<?>) value;
            if (list.isEmpty()) {
                builder.append("[]");
            } else {
                final String indent = " ".repeat(4 * rootPathOffset);
                final String itemIndent = " ".repeat(4 * (rootPathOffset + 1));
                builder.append("[\n");
                for (Object item : list) {
                    builder.append(itemIndent);
                    addJsonValue(builder, item, rootPathOffset + 1);
                    builder.append(",\n");
                }
                builder.delete(builder.length() - 2, builder.length() - 1);
                builder.append(indent).append(']');
            }
        }
    }
}
