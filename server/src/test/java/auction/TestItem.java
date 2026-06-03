package auction;

import model.item.Item;
import model.user.User;

public class TestItem extends Item {

    public TestItem(
            String name,
            User seller,
            double startPrice,
            double currentPrice
    ) {
        super(
                name,
                seller,
                startPrice,
                currentPrice
        );
    }

    @Override
    public String getType() {
        return "TEST";
    }
}