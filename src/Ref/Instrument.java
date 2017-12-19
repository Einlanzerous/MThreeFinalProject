package Ref;

import java.io.Serializable;
import java.util.Date;

public class Instrument implements Serializable{
	private long id;
	private String name;
	private Ric ric;
	private String isin;
	private String sedol;
	private String bbid;
	public Instrument(Ric ric){
		this.ric=ric;
	}
	public String toString(){
		return ric.getRic();
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Ric getRic() {
		return ric;
	}

	public String getIsin() {
		return isin;
	}

	public String getSedol() {
		return sedol;
	}

	public String getBbid() {
		return bbid;
	}
}

/*TODO
Index
bond
methods
*/