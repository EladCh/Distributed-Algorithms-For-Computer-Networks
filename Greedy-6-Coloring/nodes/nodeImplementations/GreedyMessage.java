/*
Author : Elad Chamilevsky
Id: 204086631
*/
package projects.sample6.nodes.nodeImplementations;

import sinalgo.nodes.messages.Message;

public class GreedyMessage extends Message {

	@Override
	public Message clone() {
		return this; // read-only policy 
	}
}
