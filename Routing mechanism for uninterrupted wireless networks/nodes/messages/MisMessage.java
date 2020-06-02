package projects.maman15.nodes.messages;

import sinalgo.nodes.messages.Message;

public class MisMessage extends Message {

	private int randomNum;
	private int uniqeId;
		
	@Override
	public Message clone() {
		return this; // read-only policy 
	}

	public int getUniqeId() {
		return uniqeId;
	}

	public void setUniqeId(int uniqeId) {
		this.uniqeId = uniqeId;
	}
		
	public int getRandomNum() {
		return randomNum;
	}

	public void setRandomNum(int ramdomNum_A) {
		randomNum = ramdomNum_A;
	}
}
