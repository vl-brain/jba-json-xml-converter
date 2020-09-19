package converter.json.builder;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class PropertyBuilder implements ObjectBuilder {
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private String pendingProperty;

    @Override
    public boolean isPendingFirstProperty() {
        return pendingProperty == null && properties.isEmpty();
    }

    @Override
    public boolean isPendingNextProperty() {
        return pendingProperty == null && !properties.isEmpty();
    }

    @Override
    public boolean isPendingFirstValue() {
        return pendingProperty != null;
    }

    @Override
    public boolean isPendingNextValue() {
        return false;
    }

    @Override
    public void setValue(Object value) {
        properties.put(requireNonNull(pendingProperty, "Pending property empty!"), value);
        pendingProperty = null;
    }

    @Override
    public void setProperty(String propertyName) {
        if (pendingProperty == null) {
            pendingProperty = propertyName;
        } else {
            throw new IllegalStateException("Override pending property!");
        }
    }

    @Override
    public Map<String, Object> build() {
        if (pendingProperty != null) {
            throw new IllegalStateException("Property '" + pendingProperty + "' value undefined!");
        }
        return properties;
    }
}

