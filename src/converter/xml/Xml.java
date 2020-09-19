package converter.xml;

public class Xml {

    private final XmlNode root;

    public Xml(XmlNode root) {
        this.root = root;
    }

    public XmlNode getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return root.toString();
    }

}
