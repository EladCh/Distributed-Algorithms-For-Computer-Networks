package projects.maman15.nodes.messages;

import java.util.ArrayList;
import java.util.List;

import projects.maman15.nodes.nodeImplementations.UdgNetworkNode;
import projects.maman15.nodes.timers.SmsTimer;
import sinalgo.gui.helper.NodeSelectionHandler;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class SmsMessage extends Message {
	public int senderID; // sequence ID of the sender
	public Node destination;
	public Node sender;
	public String text;
	//public SmsTimer smsTimer;
	public boolean firstTime;
	public ArrayList<Integer> path;
	public boolean MisDestination;
	public Integer MisDestinationId;
	public List<Integer> visitedMisNodes;
	
	
	public SmsMessage(int nearMisNodeID, UdgNetworkNode destinationNode,UdgNetworkNode senderNode,	String textMessage) {
		this.senderID = nearMisNodeID; 
		this.destination = destinationNode;
		this.sender = senderNode;
		this.text = textMessage;
		MisDestination = false;
		MisDestinationId = 0;
		firstTime = true;
		visitedMisNodes= new ArrayList<Integer>();
	}

	@Override
	public Message clone() {
		return this;
	}

}

