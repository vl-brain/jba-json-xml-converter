package converter.xml;

import converter.AbstractNode;

import java.util.List;
import java.util.Map;

public class XmlNode extends AbstractNode<XmlNode> {
    public XmlNode() {
    }

    public XmlNode(String name, Map<String, String> attributes, String value) {
        super(name, attributes, value);
    }

    @Override
    protected XmlNode self() {
        return this;
    }

    @Override
    public String toString() {
        final String name = getName();
        final String indent = " ".repeat(4 * getRootPathOffset());
        final StringBuilder builder = new StringBuilder(indent)
                .append('<')
                .append(name);
        getAttributes().forEach((attr, attrValue) -> builder.append(' ')
                .append(attr)
                .append("=\"")
                .append(attrValue)
                .append('"'));
        final String value = getValue();
        if (value == null) {
            final List<XmlNode> children = getChildren();
            if (children.isEmpty()) {
                builder.append("/>");
            } else {
                builder.append(">\n");
                for (XmlNode child : children) {
                    builder.append(child.toString())
                            .append('\n');
                }
                builder.append(indent)
                        .append("</")
                        .append(name)
                        .append(">");
            }
        } else {
            builder.append(">")
                    .append(value)
                    .append("</")
                    .append(name)
                    .append(">");
        }
        return builder.toString();
    }

    @Override
    public String getPathSeparator() {
        return "/";
    }
}
