package tibame.example.shopping;

import java.util.HashSet;
import java.util.Set;

/**
 * @author leo
 */
public class Cart {

    private static final Set<String> itemKeys = new HashSet<>();

    public static void addToCart(String itemKey) {
        itemKeys.add(itemKey);
    }

    public static void removeFromCart(String itemKey) {
        itemKeys.remove(itemKey);
    }

    public static Set<String> getItemKeys() {
        return itemKeys;
    }


}
