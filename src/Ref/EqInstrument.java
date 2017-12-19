package Ref;

import java.util.Date;

class EqInstrument extends Instrument{
	private Date exDividend;

	public EqInstrument(Ric ric){
		super(ric);
	}

	public Date getExDividend() {
		return exDividend;
	}

}
