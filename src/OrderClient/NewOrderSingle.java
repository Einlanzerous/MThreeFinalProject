package OrderClient;

import java.io.Serializable;

import Ref.Instrument;

public class NewOrderSingle implements Serializable{
	private int size;
	private float price;
	private Instrument instrument;

	public NewOrderSingle(int size,float price,Instrument instrument){
		this.size=size;
		this.price=price;
		this.instrument=instrument;
	}

	public int getSize() {
		return size;
	}

	public float getPrice() {
		return price;
	}

	public Instrument getInstrument() {
		return instrument;
	}
}