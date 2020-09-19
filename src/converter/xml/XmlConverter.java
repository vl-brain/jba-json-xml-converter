package converter.xml;

import converter.intermediate.Intermediate;

public class XmlConverter {

    public static Xml convert(Intermediate other) {
        return new Xml(from(other));
    }

    private static XmlNode from(Intermediate other) {
        final XmlNode node = new XmlNode(
                other.getName(),
                other.getAttributes(),
                other.getValue());
        for (Intermediate child : other.getChildren()) {
            node.addChild(from(child));
        }
        return node;
    }

}
