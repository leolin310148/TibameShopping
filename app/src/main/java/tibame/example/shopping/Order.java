package tibame.example.shopping;

/**
 * @author leo
 */
public class Order {
    private String buyerUid;

    private Item item;

    public String getBuyerUid() {
        return buyerUid;
    }

    public void setBuyerUid(String buyerUid) {
        this.buyerUid = buyerUid;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }
}
