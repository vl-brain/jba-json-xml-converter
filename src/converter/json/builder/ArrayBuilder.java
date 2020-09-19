package converter.json.builder;

import java.util.ArrayList;
import java.util.List;

public class ArrayBuilder implements ObjectBuilder {
    private final List<Object> list = new ArrayList<>();

    @Override
    public boolean isPendingFirstProperty() {
        return false;
    }

    @Override
    public boolean isPendingNextProperty() {
        return false;
    }

    @Override
    public void setProperty(String propertyName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Object value) {
        list.add(value);
    }

    @Override
    public List<Object> build() {
        return list;
    }

    @Override
    public boolean isPendingFirstValue() {
        return list.isEmpty();
    }

    @Override
    public boolean isPendingNextValue() {
        return !list.isEmpty();
    }
}
