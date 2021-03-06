package TradeScreen;

import java.io.IOException;

import OrderManager.Order;

public interface TradeScreen {
	enum api{newOrder, price, fill, cross}
	void newOrder(int id,Order order) throws IOException, InterruptedException;
	void acceptOrder(int id) throws IOException;
	void sliceOrder(int id,int sliceSize) throws IOException;
	void price(int id,Order o) throws InterruptedException, IOException;
}
