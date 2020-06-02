package projects.maman15.nodes.timers;

import projects.maman15.nodes.messages.SmsMessage;
import projects.maman15.nodes.nodeImplementations.UdgNetworkNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class SmsTimer extends Timer {
	public String text;
	public UdgNetworkNode destination; 
	public UdgNetworkNode nearMisNode;
	public String path;
	
	public boolean enabled = true;
	
	public void disable() {
		enabled = false;
	}

	public SmsTimer(String aText, UdgNetworkNode destinationNode, UdgNetworkNode aNearToMisNode) {
		this.text = aText;
		this.destination = destinationNode;
		this.nearMisNode = aNearToMisNode;
		//this.path = aPath;
	}
	
	@Override
	public void fire() {
		if(enabled) {
			UdgNetworkNode mn = (UdgNetworkNode) this.node;
			mn.setMessageSenderNode(true);
			// Assemble an SMS and send it to the next node in chain
			SmsMessage msg = new SmsMessage(nearMisNode.ID, destination, mn, text);
			this.node.send(msg, nearMisNode);
			//this.startRelative(1, this.node); // TODO: time?
		}
	}

}
