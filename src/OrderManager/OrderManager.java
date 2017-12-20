package OrderManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import Database.Database;
import LiveMarketData.LiveMarketData;
import OrderClient.NewOrderSingle;
import OrderRouter.Router;
import TradeScreen.TradeScreen;

public class OrderManager {
	private static LiveMarketData liveMarketData;
	private HashMap<Integer, Order> orders = new HashMap<Integer,Order>(); //debugger will do this line as it gives state to the object
	//currently recording the number of new order messages we get. TODO why? use it for more?
	private int id = 0; //debugger will do this line as it gives state to the object
	private Socket[] orderRouters; //debugger will skip these lines as they disappear at compile time into 'the object'/stack
	private Socket[] clients;
	private Socket trader;

	//@param args the command line arguments
	public OrderManager(InetSocketAddress[] orderRouters, InetSocketAddress[] clients,
	InetSocketAddress trader,LiveMarketData liveMarketData) throws IOException,
	ClassNotFoundException, InterruptedException {

		this.liveMarketData = liveMarketData;
		this.trader = connect(trader);
		//for the router connections, copy the input array into our object field.
		//but rather than taking the address we create a socket+ephemeral port and
		// connect it to the address
		this.orderRouters = new Socket[orderRouters.length];
		int i = 0; //need a counter for the the output array

		for (InetSocketAddress location : orderRouters) {
			this.orderRouters[i] = connect(location);
			i++;
		}

		//repeat for the client connections
		this.clients = new Socket[clients.length];
		i = 0;

		for (InetSocketAddress location : clients) {
			this.clients[i] = connect(location);
			i++;
		}
		processDay();
	}

	private void processDay() throws IOException, ClassNotFoundException {
		int clientId, routerId;
		Socket client, router;

		//main loop, wait for a message, then process it
		while(true){
			//TODO this is pretty cpu intensive, use a more modern polling/interrupt/select approach
			//we want to use the arrayindex as the clientId, so use traditional for loop instead of foreach
			for(clientId = 0; clientId < this.clients.length; clientId++){ //check if we have data on any of the sockets
				client = this.clients[clientId];

				if(0 < client.getInputStream().available()){ //if we have part of a message ready to read, assuming this doesn't fragment messages
					ObjectInputStream is = new ObjectInputStream(client.getInputStream()); //create an object inputstream, this is a pretty stupid way of doing it, why not create it once rather than every time around the loop
					String method = (String)is.readObject();
					System.out.println(Thread.currentThread().getName() + " calling " + method);

					switch(method){ //determine the type of message and process it
						//call the newOrder message with the clientId and the message (clientMessageId,NewOrderSingle)
						case "newOrderSingle":
							newOrder(clientId, is.readInt(), (NewOrderSingle)is.readObject());
							break;
						case "cancel":
							cancelOrder(is.readInt());
							break;
						default:
							System.err.println("Error- unknown message type");
					}
				}
			}

			for(routerId = 0; routerId < this.orderRouters.length; routerId++){ //check if we have data on any of the sockets
				router = this.orderRouters[routerId];

				if(0 < router.getInputStream().available()){ //if we have part of a message ready to read, assuming this doesn't fragment messages
					ObjectInputStream is = new ObjectInputStream(router.getInputStream()); //create an object inputstream, this is a pretty stupid way of doing it, why not create it once rather than every time around the loop
					String method = (String)is.readObject();
					System.out.println(Thread.currentThread().getName() + " calling " + method);

					switch(method){ //determine the type of message and process it
						case "bestPrice":
							int OrderId = is.readInt();
							int SliceId = is.readInt();
							Order slice = orders.get(OrderId).slices.get(SliceId);
							slice.bestPrices[routerId] = is.readDouble();
							slice.bestPriceCount += 1;
							if(slice.bestPriceCount == slice.bestPrices.length)
								reallyRouteOrder(SliceId, slice);
							break;
						case "newFill":
							newFill(is.readInt(), is.readInt(), is.readInt(), is.readDouble());
							break;
					}
				}
			}

			if(0 < this.trader.getInputStream().available()){
				ObjectInputStream is = new ObjectInputStream(this.trader.getInputStream());
				String method = (String)is.readObject();
				System.out.println(Thread.currentThread().getName() + " calling " + method);

				switch(method){
					case "acceptOrder":
						acceptOrder(is.readInt());
						break;
					case "sliceOrder":
						sliceOrder(is.readInt(), is.readInt());
				}
			}
		}
	}

	private Socket connect(InetSocketAddress location) throws InterruptedException{
		int tryCounter = 0;
		while(tryCounter < 600){
			try{
				Socket s = new Socket(location.getHostName(), location.getPort());
				s.setKeepAlive(true);
				return s;
			} catch (IOException e) {
				Thread.sleep(1000);
				tryCounter++;
			}
		}
		System.out.println("Failed to connect to " + location.toString());
		return null;
	}

	private void newOrder(int clientId, int clientOrderId, NewOrderSingle nos) throws IOException{
		orders.put(id, new Order(clientId, clientOrderId, nos.getInstrument(), nos.getSize()));
		//send a message to the client with 39=A; //OrdStatus is Fix 39, 'A' is 'Pending New'
		ObjectOutputStream newOrderStream = new ObjectOutputStream(clients[clientId].getOutputStream());
		//newOrderSingle acknowledgement
		//ClOrdId is 11=
		newOrderStream.writeObject("11=" + clientOrderId + ";35=A;39=A;");
		newOrderStream.flush();
		sendOrderToTrader(id, orders.get(id), TradeScreen.api.newOrder);
		//send the new order to the trading screen
		//don't do anything else with the order, as we are simulating high touch orders and so need to wait for the trader to accept the order
		id++;
	}

	private void sendOrderToTrader(int id, Order order, Object method) throws IOException{
		ObjectOutputStream traderStream = new ObjectOutputStream(trader.getOutputStream());
		traderStream.writeObject(method);
		traderStream.writeInt(id);
		traderStream.writeObject(order);
		traderStream.flush();
	}

	public void acceptOrder(int id) throws IOException{
		Order order = orders.get(id);

		if(order.OrdStatus == '4'){
			System.err.println("Order cancelled - declining.");
			return;
		}
		else if(order.OrdStatus != 'A'){ //Pending New
			System.out.println("error accepting order that has already been accepted");
			return;
		}

		order.OrdStatus = '0'; //New
		ObjectOutputStream os = new ObjectOutputStream(clients[order.clientId].getOutputStream());
		//newOrderSingle acknowledgement
		//ClOrdId is 11=
		os.writeObject("11=" + order.getClientOrderID() + ";39=0");
		os.flush();

		price(id, order);
	}

	public void sliceOrder(int id, int sliceSize) throws IOException{
		Order order = orders.get(id);
		//slice the order. We have to check this is a valid size.
		//Order has a list of slices, and a list of fills, each slice is a childorder and each fill is associated with either a child order or the original order
		if(sliceSize > order.sizeRemaining() - order.sliceSizes()){
			System.out.println("error sliceSize is bigger than remaining size to be filled on the order");
			return;
		}
		int sliceId = order.newSlice(sliceSize);
		Order slice = order.slices.get(sliceId);
		internalCross(id, slice);
		int sizeRemaining = order.slices.get(sliceId).sizeRemaining();
		if(sizeRemaining > 0){
			routeOrder(id, sliceId, sizeRemaining, slice);
		}
	}

	private void internalCross(int id, Order order) throws IOException{
		for(Map.Entry<Integer, Order> entry:orders.entrySet()){
			if (entry.getKey() == id) continue;
			Order matchingOrder = entry.getValue();

			if (!(matchingOrder.instrument.equals(order.instrument) && matchingOrder.initialMarketPrice == order.initialMarketPrice))
				continue;
			//TODO add support here and in Order for limit orders
			int sizeBefore = order.sizeRemaining();
			order.cross(matchingOrder);

			if (sizeBefore != order.sizeRemaining()) {
				sendOrderToTrader(id, order, TradeScreen.api.cross);
			}
		}
	}

	private void cancelOrder(int id){
		Order cancelOrder = orders.get(id);
		cancelOrder.OrdStatus = '4'; //cancel marker

		try {
			ObjectOutputStream newOrderStream = new ObjectOutputStream(clients[cancelOrder.clientId].getOutputStream());
			newOrderStream.writeObject("11=" + cancelOrder.clientId + ";39=4");
			newOrderStream.flush();
			System.err.println(Thread.currentThread().getName()+ " successfully cancelled order: " +id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void newFill(int id, int sliceId, int size, double price) throws IOException{
		Order order = orders.get(id);

		if(order.OrdStatus == '4'){
			System.err.println("Refusing fill; order was cancelled");
			return;
		}

		order.slices.get(sliceId).createFill(size, price);

		if(order.sizeRemaining() == 0){
			Database.write(order);
		}
		sendOrderToTrader(id, order, TradeScreen.api.fill);
	}

	private void routeOrder(int id, int sliceId, int size, Order order) throws IOException{
		for(Socket r:orderRouters){
			ObjectOutputStream routingStream = new ObjectOutputStream(r.getOutputStream());
			routingStream.writeObject(Router.api.priceAtSize);
			routingStream.writeInt(id);
			routingStream.writeInt(sliceId);
			routingStream.writeObject(order.instrument);
			routingStream.writeInt(order.sizeRemaining());
			routingStream.flush();
		}
		//need to wait for these prices to come back before routing
		order.bestPrices = new double[orderRouters.length];
		order.bestPriceCount = 0;
	}

	private void reallyRouteOrder(int sliceId, Order order) throws IOException{
		//TODO this assumes we are buying rather than selling
		int minIndex = 0;
		double min=order.bestPrices[0];

		for(int i = 1;i < order.bestPrices.length;i++){
			if(min > order.bestPrices[i]){
				minIndex = i;
				min=order.bestPrices[i];
			}
		}

		ObjectOutputStream os = new ObjectOutputStream(orderRouters[minIndex].getOutputStream());
		os.writeObject(Router.api.routeOrder);
		os.writeInt(order.id);
		os.writeInt(sliceId);
		os.writeInt(order.sizeRemaining());
		os.writeObject(order.instrument);
		os.flush();
	}
	private void sendCancel(Order order, Router orderRouter){
		//orderRouter.sendCancel(order);
		//order.orderRouter.writeObject(order);

	}
	private void price(int id, Order order) throws IOException{
		liveMarketData.setPrice(order);
		sendOrderToTrader(id, order, TradeScreen.api.price);
	}
}