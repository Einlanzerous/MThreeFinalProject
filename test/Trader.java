import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

import javax.net.ServerSocketFactory;

import OrderManager.Order;
import TradeScreen.TradeScreen;

public class Trader extends Thread implements TradeScreen{
	private static Socket omConn;
	private HashMap<Integer,Order> orders = new HashMap<Integer,Order>();
	private int port;
	private ObjectOutputStream os;

	Trader(String name, int port){
		this.setName(name);
		this.port = port;
	}

	public void run(){
		try {
			omConn = ServerSocketFactory.getDefault().createServerSocket(port).accept();

			while(true){
				if(0 < omConn.getInputStream().available()){
					ObjectInputStream is = new ObjectInputStream(omConn.getInputStream());
					api method = (api) is.readObject();

					System.out.println("\u001B[32m" + Thread.currentThread().getName() + " calling: " + method + "\u001B[0m");

					switch(method){
						case newOrder:
							newOrder(is.readInt(), (Order) is.readObject());
							break;
						case price:
							price(is.readInt(), (Order) is.readObject());
							break;
						case cross:
							System.err.println("WAS GIVEN A CROSS- NOT IMPLEMENTED");
							is.readInt();
							is.readObject();
							break; //TODO
						case fill:
							fill(is.readInt(), (Order) is.readObject());
							break; //TODO
					}
				} else{
					//System.out.println("Trader Waiting for data to be available - sleep 1s");
					Thread.sleep(1000);
				}
			}
		} catch (IOException | ClassNotFoundException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void newOrder(int id, Order order) throws IOException, InterruptedException {
		//TODO the order should go in a visual grid, but not needed for test purposes
		Thread.sleep(2134);
		orders.put(id, order);
		acceptOrder(id);
	}


	public void acceptOrder(int id) throws IOException {
		os = new ObjectOutputStream(omConn.getOutputStream());
		os.writeObject("acceptOrder");
		os.writeInt(id);
		os.flush();
	}


	public void sliceOrder(int id, int sliceSize) throws IOException {
		os = new ObjectOutputStream(omConn.getOutputStream());
		os.writeObject("sliceOrder");
		os.writeInt(id);
		os.writeInt(sliceSize);
		os.flush();
	}

	public void price(int id, Order order) throws InterruptedException, IOException {
		//TODO should update the trade screen
		Thread.sleep(2134);
		sliceOrder(id,orders.get(id).sizeRemaining()/2);
	}

	public void fill(int id, Order order) throws IOException {
		System.out.println("\u001B[32m" + Thread.currentThread().getName() +
				" is filling " + order.sizeRemaining() + "\u001B[0m");
		os = new ObjectOutputStream(omConn.getOutputStream());
		os.writeObject("newFill");
		os.writeInt(id);
		os.writeInt(0);
		os.writeInt(order.sizeRemaining());
		os.writeDouble(order.initialMarketPrice);
		os.flush();
	}
}
