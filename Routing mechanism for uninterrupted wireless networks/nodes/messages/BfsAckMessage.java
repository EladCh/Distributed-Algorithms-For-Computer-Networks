package projects.maman15.nodes.messages;

import java.util.ArrayList;
import java.util.Set;

import sinalgo.nodes.messages.Message;

public class BfsAckMessage extends Message {

	private int initatorMisNodeId;
	private int fatherNodeId;
	private int ackingNodeId;
	private int nodeLayer;
	private ArrayList<Integer> path = new ArrayList<Integer>();
	
		
	@Override
	public Message clone() {
		return this; // read-only policy 
	}


	public int getFatherNodeId() {
		return fatherNodeId;
	}


	public void setFatherNodeId(int fatherNodeId) {
		this.fatherNodeId = fatherNodeId;
	}


	public int getAckingNodeId() {
		return ackingNodeId;
	}


	public void setAckingNodeId(int ackingNodeId) {
		this.ackingNodeId = ackingNodeId;
	}


	public int getNodeLayer() {
		return nodeLayer;
	}


	public void setNodeLayer(int nodeLayer) {
		this.nodeLayer = nodeLayer;
	}


	public int getInitatorMisNodeID() {
		return initatorMisNodeId;
	}


	public void setInitatorMisNodeID(int initatorMisNode) {
		this.initatorMisNodeId = initatorMisNode;
	}


	public void addToPath(int id) {
		path.add((Integer)id);
	}

	public ArrayList<Integer> getPath() {
		return path;
	}
}
