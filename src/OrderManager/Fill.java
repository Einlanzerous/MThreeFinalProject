package OrderManager;

import java.io.Serializable;

class Fill implements Serializable {
	//long id;
	int size; // make private add getter
	double price;

	Fill(int size,double price){
		this.size=size;
		this.price=price;
	}

}
