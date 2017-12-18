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
class EqInstrument extends Instrument{
	private Date exDividend;

	public EqInstrument(Ric ric){
		super(ric);
	}

	public Date getExDividend() {
		return exDividend;
	}

}
class FutInstrument extends Instrument{
	private Date expiry;
	private Instrument underlier;

	public FutInstrument(Ric ric){
		super(ric);
	}

	public Date getExpiry() {
		return expiry;
	}

	public Instrument getUnderlier() {
		return underlier;
	}
}
/*TODO
Index
bond
methods
*/