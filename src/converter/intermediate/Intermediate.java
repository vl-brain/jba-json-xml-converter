package converter.intermediate;

import converter.AbstractNode;

import java.util.List;
import java.util.Map;

public class Intermediate extends AbstractNode<Intermediate> {
    Intermediate(String name) {
        super(name);
    }

    Intermediate(String name, Map<String, String> attributes, String value) {
        super(name, attributes, value);
    }

    private static String formatAttrValue(Object value) {
        if (value == null) {
            return "\"\"";
        }
        return formatValue(value);
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return "\"" + value + "\"";
        }
        throw new IllegalArgumentException("Simple type expected!");
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("\nElement:\npath = ")
                .append(getPath());
        final List<Intermediate> children = getChildren();
        if (children.isEmpty()) {
            builder.append("\nvalue = ")
                    .append(formatValue(getValue()));
        }
        final Map<String, String> attributes = getAttributes();
        if (!attributes.isEmpty()) {
            builder.append("\nattributes:\n");
            attributes.forEach((attr, attrValue) -> builder.append(attr)
                    .append(" = ")
                    .append(formatAttrValue(attrValue)));
        }
        return builder.toString();
    }

    public void view() {
        view(this);
    }

    private static void view(Intermediate node) {
        System.out.println(node);
        for (Intermediate child : node.getChildren()) {
            view(child);
        }
    }

    @Override
    protected Intermediate self() {
        return this;
    }

    @Override
    public String getPathSeparator() {
        return ", ";
    }
}
