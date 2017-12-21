import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;
import OrderManager.Order;
import OrderRouter.Router;
import Ref.Instrument;
import Ref.Ric;
import javax.net.ServerSocketFactory;

public class SampleRouter extends Thread implements Router{
	private static final Random RANDOM_NUM_GENERATOR = new Random();
	private static final Instrument[] INSTRUMENTS = {new Instrument(new Ric("VOD.L")), new Instrument(new Ric("BP.L")), new Instrument(new Ric("BT.L"))};
	private Socket omConn;
	private int port;
	private ObjectOutputStream os;


	public SampleRouter(String name, int port){
		this.setName(name);
		this.port = port;
	}

	public void run(){
        ObjectInputStream is;
		//OM will connect to us
		try {
			omConn = ServerSocketFactory.getDefault().createServerSocket(port).accept();

			while(true){
				if(omConn.getInputStream().available() > 0){
					is=new ObjectInputStream(omConn.getInputStream());
					Router.api methodName = (Router.api)is.readObject();
					System.out.println("\u001B[35m" + "Order Router received method call for: " + methodName + "\u001B[0m");

					switch(methodName){
						case routeOrder:
							routeOrder(is.readInt(), is.readInt(), is.readInt(), (Instrument)is.readObject());
							break;
						case priceAtSize:
							priceAtSize(is.readInt(), is.readInt(), (Instrument)is.readObject(), is.readInt());
							break;
					}
				}
				else
					Thread.sleep(100);
			}
		} catch (IOException | ClassNotFoundException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void routeOrder(int id, int sliceId, int size, Instrument i) throws IOException, InterruptedException {
		int fillSize = RANDOM_NUM_GENERATOR.nextInt(size);
		double fillPrice = 199 * RANDOM_NUM_GENERATOR.nextDouble();
		System.out.format("\u001B[35m*** SampleRouter "+ Thread.currentThread().getName()+
				" routing "+ fillSize+" with fill price of: $%.2f " + "\u001B[37m***\n",fillPrice);
		Thread.sleep(42);
		os = new ObjectOutputStream(omConn.getOutputStream());
		os.writeObject("newFill");
		os.writeInt(id);
		os.writeInt(sliceId);
		os.writeInt(fillSize);
		os.writeDouble(fillPrice);
		os.flush();
	}

	public void sendCancel(int id, int sliceId, int size, Instrument i){

	}

	public void priceAtSize(int id, int sliceId, Instrument i, int size) throws IOException{
		os = new ObjectOutputStream(omConn.getOutputStream());
		os.writeObject("bestPrice");
		os.writeInt(id);
		os.writeInt(sliceId);
		double priceSize = 199 * RANDOM_NUM_GENERATOR.nextDouble();
		os.writeDouble(priceSize);
		System.out.format("\u001B[35m" + "*** SampleRouter "+ Thread.currentThread().getName()+
				" priceAtSize: $%.2f" + "\u001B[37m ***\n",priceSize);
		os.flush();
	}
}
