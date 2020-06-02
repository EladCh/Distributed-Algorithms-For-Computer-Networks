package projects.maman15.nodes.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sinalgo.nodes.messages.Message;

public class BfsReqMessage extends Message {

	private int senderMisNodeId = 0;
	private int bfsLayerNum = 0;
	private ArrayList<Set<Integer>> nodesDistancesArray = new ArrayList<Set<Integer>>();
	public ArrayList<Integer> visitedNods = new ArrayList<Integer>();
	
	public Map<Integer, ArrayList<Integer>> nodeByLayersMap=new HashMap<Integer, ArrayList<Integer>>(); 
	public Map<Integer, Integer> layerByNodeMap=new HashMap<Integer, Integer>(); 
	
	public Map<Integer, ArrayList<Integer>> sonsByFatherMap=new HashMap<Integer, ArrayList<Integer>>();
	public Map<Integer, Integer> fatherBySonMap=new HashMap<Integer, Integer>();

	@Override
	public Message clone() {
		return this; // read-only policy 
	}

	public int getMisSenderNodeId() {
		return senderMisNodeId;
	}

	public void setMisSenderNodeId(int senderNodeId) {
		this.senderMisNodeId = senderNodeId;
	}

	public int getBfsLayerNum() {
		return bfsLayerNum;
	}

	public void setBfsLayerNum(int bfsRoundNum) {
		this.bfsLayerNum = bfsRoundNum;
	}
	
	/*public boolean isThisNodeWasAlreadyVisited(int ID) {
		for(Set<Integer> s: nodesDistancesArray) {
			//convert set to object arr
			Object[] layer = s.toArray();
			Integer[] tempLayerArr = new Integer[layer.length];
			//convert object arr to int arr
			System.arraycopy(layer, 0, tempLayerArr, 0, layer.length);
			List<Integer> intLayerArr = new ArrayList<Integer>();
			//convert int arr to int list
			for (int i : tempLayerArr)
			{
				intLayerArr.add(i);
			}
			//check if the given id exists in that layer
			if(intLayerArr.contains(ID)) {
				return true;
			}
		}
		return false;
	}*/

	public void addNodeAndLayerToMaps(int nodeId) {
		//add to map <layer num, nodes in that layer>
		ArrayList<Integer> LayerNodesIdArr = nodeByLayersMap.get(bfsLayerNum);
		
		if(LayerNodesIdArr != null)
			LayerNodesIdArr.add(nodeId);
		else {
			LayerNodesIdArr = new ArrayList<Integer>();
			LayerNodesIdArr.add(nodeId);
		}
		
		nodeByLayersMap.put(bfsLayerNum,LayerNodesIdArr);
		
		//add to map <node id, the layer of that node>
		layerByNodeMap.put(nodeId,bfsLayerNum);
	}

	public void setFatherSonReleationshipMap(int nodeId, int senderId) {
		// nodeId is the son, senderId is the father
		
		//add to map <father, sons>
		ArrayList<Integer> sonsIdArr = sonsByFatherMap.get(senderId);
		
		if(sonsIdArr != null)
			sonsIdArr.add(nodeId);
		else {
			sonsIdArr = new ArrayList<Integer>();
			sonsIdArr.add(nodeId);
		}
		
		sonsByFatherMap.put(senderId,sonsIdArr);
		
		//add to map <node id, the layer of that node>
		fatherBySonMap.put(nodeId,senderId);
		
	}
	
}
