import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

import OrderClient.Client;
import OrderClient.NewOrderSingle;
import OrderManager.Order;
import Ref.Instrument;
import Ref.Ric;

public class SampleClient extends Thread implements Client {
	private static final Random RANDOM_NUM_GENERATOR = new Random();
	private static final Instrument[] INSTRUMENTS = {new Instrument(new Ric("VOD.L")), new Instrument(new Ric("BP.L")), new Instrument(new Ric("BT.L"))};
	private static final HashMap OUT_QUEUE = new HashMap(); //queue for outgoing orders
	private int id = 0; //message id number
	private Socket omConn; //connection to order manager
			
	public SampleClient(String name, int port) throws IOException{
		//OM will connect to us
		omConn = new ServerSocket(port).accept();
		this.setName(name);

		System.out.println("OM connected to client port " + port);
	}

	public int sendOrder()throws IOException{
		int size = RANDOM_NUM_GENERATOR.nextInt(5000);
		int instid = RANDOM_NUM_GENERATOR.nextInt(3);
		Instrument instrument = INSTRUMENTS[RANDOM_NUM_GENERATOR.nextInt(INSTRUMENTS.length)];
		NewOrderSingle nos = new NewOrderSingle(size, instid, instrument);
		
		show("sendOrde r: id=" + id + " size=" + size + " instrument=" + INSTRUMENTS[instid].toString());
		OUT_QUEUE.put(id, nos);

		if(omConn.isConnected()){
			ObjectOutputStream os = new ObjectOutputStream(omConn.getOutputStream());
			os.writeObject("newOrderSingle");
			//os.writeObject("35=D;"); //Uncommenting this will ruin the code. lol.
			os.writeInt(id);
			os.writeObject(nos);
			os.flush();
		}
		return id++;
	}

	public void sendCancel(int idToCancel) {
		show("sendCancel: id=" + idToCancel);

		if(omConn.isConnected()){
			//OMconnection.sendMessage("cancel",idToCancel);
		}
	}

	public void partialFill(Order order){
		show("Partial Fill " + order);
	}

	public void fullyFilled(Order order){
		show("Order filled" + order);
		OUT_QUEUE.remove(order.getClientOrderID());
	}

	public void cancelled(Order order){
		show("Order cancelled" + order);
		OUT_QUEUE.remove(order.getClientOrderID());
	}

	public void messageHandler(){
		ObjectInputStream is;
		char MsgType;
		int OrdStatus;

		try {
			InputStream s = omConn.getInputStream();

			while(true){
				//s.wait(); //this throws an exception!!
				while(0 < omConn.getInputStream().available()){
					is = new ObjectInputStream(omConn.getInputStream());
					String fix = (String)is.readObject();

					System.out.println(Thread.currentThread().getName() + " received fix message: " + fix);

					String[] fixTags = fix.split(";");
					int OrderId =- 1;
					//String[][] fixTagsValues = new String[fixTags.length][2];
					for (String fixTag : fixTags) {
						String[] tag_value = fixTag.split("=");
						switch (tag_value[0]) {
							case "11":
								OrderId = Integer.parseInt(tag_value[1]);
								System.out.println("Case 11 apparently");
								break;
							case "35":
								MsgType = tag_value[1].charAt(0);
								if (MsgType == 'A') newOrderSingleAcknowledgement(OrderId);
								break;
							case "39":
								OrdStatus = tag_value[1].charAt(0);
								System.out.println("Order status is apparently: " + OrdStatus);
								break;
						}
					}
					/*
					message = connection.getMessage();
					char type;
					switch(type){
						case 'C':
							cancelled(message);
							break;
						case 'P':
							partialFill(message);
							break;
						case 'F':
							fullyFilled(message);
					}*/
					//show("");
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void show(String out) {
		System.err.println(Thread.currentThread().getName() + ":" + out);
	}

	void newOrderSingleAcknowledgement(int OrderId){
		System.out.println(Thread.currentThread().getName() + " called newOrderSingleAcknowledgement for Order ID:" + OrderId);
		//do nothing, as not recording so much state in the NOS class at present
	}
/*listen for connections
once order manager has connected, then send and cancel orders randomly
listen for messages from order manager and print them to stdout.*/
}