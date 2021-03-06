import java.io.IOException;

class MockClient extends Thread{
	private int port;

	MockClient(String name,int port){
		this.port = port;
		this.setName(name);
	}

	public void run(){
		try {
			SampleClient client = new SampleClient(Thread.currentThread().getName(), port);

			if(port == 2000){
				//TODO why does this take an arg?
				//client.sendOrder(null);
				int id = client.sendOrder();
				//client.sendCancel(id);
				client.messageHandler();
			} else{
				client.sendOrder();
				client.messageHandler();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
