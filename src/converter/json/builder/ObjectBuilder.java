package converter.json.builder;

public interface ObjectBuilder {
    boolean isPendingFirstProperty();

    boolean isPendingNextProperty();

    void setProperty(String propertyName);

    boolean isPendingFirstValue();

    boolean isPendingNextValue();

    void setValue(Object value);

    Object build();
}
