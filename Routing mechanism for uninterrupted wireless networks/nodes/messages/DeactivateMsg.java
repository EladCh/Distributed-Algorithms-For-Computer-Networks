package projects.maman15.nodes.messages;

import sinalgo.nodes.messages.Message;

public class DeactivateMsg extends Message {

	@Override
	public Message clone() {
		return this; // read-only policy 
	}

}
