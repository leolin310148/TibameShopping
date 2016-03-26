package tibame.example.shopping;

/**
 * @author leolin
 */
public class Order {

    public static final int STATUS_PROCESSING = 0;

    public static final int STATUS_CANCEL = -1;

    public static final int STATUS_SHIPPING = 1;

    private String buyerUserUid;

    private Item item;

    private int status;

    private String key;

    public String getBuyerUserUid() {
        return buyerUserUid;
    }

    public void setBuyerUserUid(String buyerUserUid) {
        this.buyerUserUid = buyerUserUid;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
