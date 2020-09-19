package converter;

import java.util.List;
import java.util.Map;

public interface Node<T extends Node<T>> {
    String getName();

    void setName(String name);

    String getValue();

    void setValue(String value);

    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    T getParent();

    void setParent(T parent);

    List<T> getChildren();

    void addChild(T node);

    void setChildren(List<T> children);

    default String getPath() {
        final T parent = getParent();
        final String name = getName();
        if (parent == null) {
            return name;
        } else {
            return parent.getPath() + getPathSeparator() + name;
        }
    }

    String getPathSeparator();

    default int getRootPathOffset() {
        int offset = 0;
        T parent = getParent();
        while (parent != null) {
            parent = parent.getParent();
            offset++;
        }
        return offset;
    }
}
