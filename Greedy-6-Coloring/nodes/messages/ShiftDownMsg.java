package projects.sample6.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * A message sent to children that should be marked.
 */
public class ShiftDownMsg extends Message {

	private int roundRecieved;

	@Override
	public Message clone() {
		return this; // read-only policy 
	}

	public void setRound(int roundCounter) {
		this.roundRecieved = roundCounter;
	}

	public int getRound() {
		return this.roundRecieved;
	}

}
