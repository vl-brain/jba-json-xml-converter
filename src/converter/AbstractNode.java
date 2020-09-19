package converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractNode<T extends Node<T>> implements Node<T> {
    private String name;
    private Map<String, String> attributes;
    private String value;
    private List<T> children;
    private T parent;

    protected AbstractNode() {
    }

    public AbstractNode(String name) {
        this.name = name;
    }

    protected AbstractNode(String name, Map<String, String> attributes, String value) {
        this.name = name;
        this.attributes = attributes;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes == null ? Map.of() : attributes;
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public List<T> getChildren() {
        return children == null ? List.of() : children;
    }

    @Override
    public void addChild(T child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        child.setParent(self());
    }

    @Override
    public void setChildren(List<T> children) {
        this.children = children;
        for (T child : children) {
            child.setParent(self());
        }
    }

    protected abstract T self();

    @Override
    public T getParent() {
        return parent;
    }

    @Override
    public void setParent(T parent) {
        this.parent = parent;
    }

}
