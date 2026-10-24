package restaurant.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Menu {

    // the order matters, the food rule picks items by position
    private final List<MenuItem> items = new ArrayList<>();

    public void addItem(MenuItem item) {
        items.add(item);
    }

    public MenuItem getItem(int index) {
        return items.get(index);
    }

    public int size() {
        return items.size();
    }

    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
