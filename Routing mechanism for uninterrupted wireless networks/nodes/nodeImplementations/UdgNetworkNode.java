package projects.maman15.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JOptionPane;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.maman15.CustomGlobal;
import projects.maman15.nodes.messages.BfsAckMessage;
import projects.maman15.nodes.messages.BfsReqMessage;
import projects.maman15.nodes.messages.MisMessage;
import projects.maman15.nodes.messages.SmsMessage;
import projects.maman15.nodes.timers.SmsTimer;

import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.helper.NodeSelectionHandler;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.Node.NodePopupMethod;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class UdgNetworkNode extends Node {
	
	// a list of all antennas
	//private static Vector<UdgNetworkNode> antennaList = new Vector<UdgNetworkNode>();
	public static int tValue = 7;
	public static List<UdgNetworkNode> MIS_set = new ArrayList<UdgNetworkNode>();
	public static List<Integer> MIS_set_ids = new ArrayList<Integer>();
	public static int maxDegInGraph = 0;
	public static int maxRoutingTableSize;
	public static int numOfActiveNodes;
	public static int misConstructionFinishRound = 99999;
	
	private static int radius;
	private static int N; //number of nodes in graph
	
	public List<Integer> visitedMisNodeBfsRequests = new  ArrayList<Integer>();
	public UdgNetworkNode nearMisNodeForSend = null;
	public UdgNetworkNode destinationNode = null;
	public String textMessage;
	
	private int roundNum = 0;
	private int randomNum = 0;
	private int roundInBfsConstruction = 0;
	
	private boolean isActive = true;
	private boolean bfsConstructionDone = false;
	private boolean thisNodeInMIS = false;
	private boolean messageRecieved = false;
	private boolean messageSenderNode = false;
	private boolean messageDestinationNode = false;
	
	
	/**
	 * the map looks like this (MisRootId, routingMap). in every node
	 */
	private Map<Integer, Map<Integer, UdgNetworkNode>> bfsRoutingMapByRootId=new HashMap<Integer, Map<Integer, UdgNetworkNode>>(); 
	
	/**
	 * the map looks like this (nodeId, distance from me).
	 */
	private Map<Integer, Integer> distanceFromMe =new HashMap<Integer, Integer>(); 
	
	/**
	 * the map looks like this (nodeId, father). this map exists only in mis nodes
	 */
	private Map<Integer, Integer> misNodeRoutingMap=new HashMap<Integer, Integer>();
	
	
	
	
	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}

	
	@Override
	public void handleMessages(Inbox inbox) {
		//System.out.println("id = " + this.ID + " active: " + this.isActive);//debug
		while(inbox.hasNext()) {
			Message msg = inbox.next();
			if(msg instanceof MisMessage) {
				UdgNetworkNode sender = (UdgNetworkNode) inbox.getSender();
				
				//phase 1: random (the random numbers are set in the preRound method)
				if(roundNum < tValue && isActive) {
					ArrayList<Integer> neighboursRandNumbers = new ArrayList<Integer>();
					int msgRandNum = sender.randomNum;
					if(msgRandNum > 0)
						neighboursRandNumbers.add(msgRandNum);
					
					//get the random number from all the neighbors
					while(inbox.hasNext()) {
						msg = inbox.next();
						if(msg instanceof MisMessage){
							sender = (UdgNetworkNode) inbox.getSender();
							msgRandNum = sender.randomNum;
							if(msgRandNum > 0)
								neighboursRandNumbers.add(msgRandNum);
						}
					}
					
					randomStage(neighboursRandNumbers);
				}
			}
			
			if(msg instanceof BfsReqMessage) {
				UdgNetworkNode sender = (UdgNetworkNode) inbox.getSender();
				BfsReqMessage Bmsg = (BfsReqMessage)msg; 
				
				List<UdgNetworkNode> sendToNextLayerNeighbors = new ArrayList<UdgNetworkNode>();
				
				if(maxDegInGraph < this.outgoingConnections.size()) {
					maxDegInGraph = this.outgoingConnections.size();
				}
				
				//send that msg to all neighbors in the next level (not already visited)
				Iterator<Edge> iterator = this.outgoingConnections.iterator();
				iterator.forEachRemaining(edge -> {UdgNetworkNode neighbor = (UdgNetworkNode)edge.endNode;
													int neighborId = neighbor.ID;
													//if not visited in this node add it to the potential next layer
													if(!Bmsg.visitedNods.contains(neighborId) /*&& !neighbor.visitedMisNodeBfsRequests.contains(Bmsg.getMisSenderNodeId())*/)
														sendToNextLayerNeighbors.add(neighbor);});
				
				//System.out.println("forward-> message initiated in Mis node: " +Bmsg.getMisSenderNodeId() + " came from node: "+sender.ID + " this node id: " + this.ID + " valid next neighbors:" + sendToNextLayerNeighbors);
				
				//already visited with this root's msg
				if(Bmsg.visitedNods.contains(this.ID)) {
					//send ack backwards to inform the mis node of the rout
					BfsAckMessage bAck = new BfsAckMessage();
					bAck.setAckingNodeId(sender.ID);
					bAck.setInitatorMisNodeID(Bmsg.getMisSenderNodeId());
					bAck.setNodeLayer(Bmsg.getBfsLayerNum()-1);
					//System.out.println("backward to sender-> message initiated in Mis node " +Bmsg.getMisSenderNodeId() + " returns from node "+this.ID + " sent back to node: " + sender.ID);
					send(bAck, sender);	
					return;
				}
				
				//first Time Visited By This Root
				else {
					//update the layer
					Bmsg.visitedNods.add(this.ID);
										
					//if there is a routing map of this MIS initiator ID add <nodeID,father> to the map
					if(this.bfsRoutingMapByRootId.get(Bmsg.getMisSenderNodeId()) != null) {
						Map<Integer, UdgNetworkNode> routingMap = this.bfsRoutingMapByRootId.get(Bmsg.getMisSenderNodeId());
						routingMap.put(this.ID, sender); //the map looks like this <nodeId, father>
						this.bfsRoutingMapByRootId.put(Bmsg.getMisSenderNodeId(), routingMap);
					}
					//new routing map of this MIS initiator ID
					else {
						//create new table for this root id and insert a new routing map with this <nodeID,father>
						Map<Integer, UdgNetworkNode> routingMap = new HashMap<Integer, UdgNetworkNode>();
						routingMap.put(this.ID, sender); //the map looks like this <nodeId, father>
						this.bfsRoutingMapByRootId.put(Bmsg.getMisSenderNodeId(), routingMap); //the map looks like this <MisRootId, routingMap>
					}
					
					//converge cast to root by prev layer (father)
					BfsAckMessage bAck = new BfsAckMessage();
					bAck.setAckingNodeId(this.ID);
					bAck.setFatherNodeId(sender.ID);
					bAck.setInitatorMisNodeID(Bmsg.getMisSenderNodeId());
					bAck.setNodeLayer(Bmsg.getBfsLayerNum());
					bAck.addToPath(this.ID);
					send(bAck, sender);		
					
					//try to explore next layer- send bcast to the neighbors in the next layer
					Bmsg.setBfsLayerNum(Bmsg.getBfsLayerNum()+1);
					for(UdgNetworkNode neighbor : sendToNextLayerNeighbors)
					{
						send(Bmsg, neighbor);
					}
					
				}
			}
			if(msg instanceof BfsAckMessage) {
				UdgNetworkNode sender = (UdgNetworkNode) inbox.getSender();
				BfsAckMessage BAmsg = (BfsAckMessage)msg; 
				
				//System.out.println("backward-> message initiated in Mis node: " +BAmsg.getInitatorMisNodeID()+" first acked from "+ BAmsg.getAckingNodeId() + " came from node: "+sender.ID + " this node id: " + this.ID);
				
				//if it is the root of this bfs construction
				if(BAmsg.getInitatorMisNodeID()==this.ID) {
					BAmsg.addToPath(this.ID);
					updateRoutingTable(BAmsg);
				}
				
				//else- an inside node of the root 
				else {
					//if there is a map to this bfs tree rooted in the InitatorMisNodeID
					if(this.bfsRoutingMapByRootId.containsKey(BAmsg.getInitatorMisNodeID())) {
						Map<Integer, UdgNetworkNode> rMap = this.bfsRoutingMapByRootId.get(BAmsg.getInitatorMisNodeID());
						UdgNetworkNode father = rMap.get(this.ID);
						BAmsg.addToPath(this.ID);
						send(BAmsg, father);
					}
					else {
						//System.out.println("!!!!!!!!!!!!Error in handleMessages:BfsAckMessage- there isn't a map to this bfs tree rooted in the InitatorMisNodeID !!!!!!!!!!!!!!!!!!!!!!");
					}
				}
			}
			
			else if(msg instanceof SmsMessage) {
				SmsMessage sms = (SmsMessage) msg;
				
				messageRecieved = true;
				
				//reached destination
				if(this.ID == sms.destination.ID) {
					this.messageDestinationNode = true;
					JOptionPane.showMessageDialog(null, "There message reached its destination!");
					return;
				}
				
				if(isNeighbor(sms.destination)) {
					this.send(sms, sms.destination); // forward the message to the destination
				}
				
				else if(isNeighbor(sms.MisDestinationId)) {
					UdgNetworkNode nextMisNode = getNextNodeObject(sms.MisDestinationId);
					if(nextMisNode != null)
						this.send(sms, nextMisNode);
				}
				
				//mis node- update the path to the destination
				 else if(thisNodeInMIS) {
					sms.visitedMisNodes.add(this.ID);
					 
					if(sms.firstTime || (sms.MisDestination && sms.MisDestinationId == this.ID)) {
						sms.MisDestination = false;
						
						//check if there is a rout to the destination node
						sms.path = getMsgPath(sms.destination.ID,this);
						
						//no path was found- try sending to a near Mis node. maybe it has a route to the destination
						if(sms.path == null) {
							JOptionPane.showMessageDialog(null, "There is no route between these two nodes");
							//SendToNeighborMisNode(sms);
						}
						
						//there is a rout- forward the message
						//delete the first step
						if(sms.firstTime || (sms.path != null && sms.path.get(sms.path.size()-1) == this.ID)) {
							if(sms.path != null) {
								Integer next = getNextNode(sms.path);}}
						sms.firstTime = false;
					}
						
					//check for extended rout by intermediate mis nodes
					ArrayList<Integer> interMisRout = getMsgPath(sms.destination.ID,this);
					if(interMisRout != null) {
						if(interMisRout.size()>=sms.path.size()) {
							sms.path=interMisRout;
							Integer next = getNextNode(sms.path);
						}
					}
					
					// forward the message to the destination
					if( sms.path !=null  && !sms.path.isEmpty()) {
						Integer next = getNextNode(sms.path);
						if(next!=null) {
							UdgNetworkNode  nextObj = getNextNodeObject(next);
							if(nextObj!=null)
								this.send(sms, nextObj); 
						}
					}
				}
				
				//regular node- forward the message to the destination
				else {
					if(!sms.path.isEmpty() && sms.path !=null) {
						Integer next = getNextNode(sms.path);
						if(next!=null) {
							UdgNetworkNode nextObj = getNextNodeObject(next);
							if(nextObj!=null)
								this.send(sms, nextObj);
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * if there is no route to the destination check if there is a route 
	 * to a neighbor Mis node which may have a route to the destination
	 * get the path to this MIS node and start the path to it.
	 * @param sms 
	 */
	private void SendToNeighborMisNode(SmsMessage sms) {
		int minDistance = N;
		Integer newMisToDestination = null;
		
		for (Map.Entry<Integer, Integer> entry : misNodeRoutingMap.entrySet()) {
			Integer key = entry.getKey();
			Integer value = entry.getValue();
			
			if(MIS_set_ids.contains(key) && !sms.visitedMisNodes.contains(key)) {
				if(distanceFromMe.get(key) <= minDistance) {
					minDistance = distanceFromMe.get(key);
					newMisToDestination = key;
				}
			}
		}
		
		if(newMisToDestination != null) {
			sms.path = getMsgPath(newMisToDestination, this);
			sms.MisDestinationId = newMisToDestination;
			sms.MisDestination = true;
		}
		else {
			JOptionPane.showMessageDialog(null, "There is no route between these two nodes");
		}
	}

	/**
	 * gets an Integer ID and returns the node object with this ID
	 * @param next
	 * @return the object of the node with this ID
	 */
	private UdgNetworkNode  getNextNodeObject(Integer next) {
		ArrayList<UdgNetworkNode> neighborObj = new ArrayList<UdgNetworkNode>();
		
		Iterator<Edge> iterator = this.outgoingConnections.iterator();
		iterator.forEachRemaining(edge -> {if(edge!=null) {UdgNetworkNode neighbor = (UdgNetworkNode)edge.endNode;
											int neighborId = neighbor.ID;
											if(neighborId==next) {
												neighborObj.add(neighbor);
											}}});
		if(neighborObj.isEmpty())
			return null;
		return neighborObj.get(0);
	}

	
	/**
	 * gets a path in an array<Integer> format and returns the next node ID
	 * @param path
	 * @return the ID of next node in the route
	 */
	private Integer getNextNode(ArrayList<Integer> path) {
		if(path.isEmpty())
			return 0;
		else {
			if(path.size()-1>= 0) {
			 Integer next = path.get(path.size()-1);
			 path.remove(path.size()-1);
			 return next;}
			else
				return 0;
		}
	}

	
	/**
	 * update Mis's routing table according to the received acks
	 * @param bAmsg
	 */
	private void updateRoutingTable(BfsAckMessage bAmsg) {
		if(!thisNodeInMIS)
			return;
		
		//run over the path and add the relations to its map
		ArrayList<Integer> path = bAmsg.getPath();
		Collections.reverse(path); //from the root to the leaves
		
		if(misNodeRoutingMap == null) {
			//System.out.println("mis node id = " + this.ID +"routingMap == null");
			misNodeRoutingMap = new HashMap<Integer, Integer>();
		}
		
		if(path.size()==1) {
			misNodeRoutingMap.put(path.get(1),path.get(0));
		}
		else {
			for(int i = 1; i< path.size(); i++) { //element in 0 is the root itself
				if(!misNodeRoutingMap.containsKey(path.get(i))) {
					misNodeRoutingMap.put(path.get(i),path.get(i-1));
				}
				else {
					int a = N;
				}
			}
		}
		
		//update distances
		this.distanceFromMe.put(bAmsg.getAckingNodeId(), bAmsg.getNodeLayer());
		
		if(maxRoutingTableSize < misNodeRoutingMap.size()) {
			maxRoutingTableSize = misNodeRoutingMap.size();
		}	
	}

	
	/**
	 * executes the deterministic stage of the construction of the MIS.
	 * the method is executed 1 time. 
	 */
	private void deterministicStage() {
		List<UdgNetworkNode> neighborsWithGreaterId = new ArrayList<UdgNetworkNode>();
		
		//iterate over the neighbors of this node and get their id if they have a greater id than that node owns
		Iterator<Edge> iterator = this.outgoingConnections.iterator();
		iterator.forEachRemaining(edge -> {UdgNetworkNode neighbor = (UdgNetworkNode)edge.endNode;
											int neighborId = neighbor.ID;
											if(neighborId > this.ID)
												neighborsWithGreaterId.add(neighbor);});
		if(neighborsWithGreaterId.isEmpty())
		{
			//this node has the greatest id in its neighborhood. add this node to the MIS
			MIS_set.add(this);
			MIS_set_ids.add(this.ID);
			thisNodeInMIS = true; 
			isActive = false;
			numOfActiveNodes--;
		}
		else
		{
			boolean hasActiveNeighbor = false;
			for(UdgNetworkNode neighbor : neighborsWithGreaterId)
			{
				if(neighbor.isActive==true) {
					hasActiveNeighbor = true;
					break;
				}
				else if(MIS_set.contains(neighbor)) {
					this.isActive = false;
					numOfActiveNodes--;
					break;
				}
			}
			//if all greater id neighbors are not active and none of them is in the MIS
			if(!hasActiveNeighbor && this.isActive == true) {
				MIS_set.add(this);
				MIS_set_ids.add(this.ID);
				thisNodeInMIS = true;
				isActive = false;
				numOfActiveNodes--;
			}						
		}	
	}
	
	
	/**
	 * executes the random stage of the construction of the MIS.
	 * the method is executed t times (t value was given by the user).
	 * @param neighboursRandNumbers 
	 */
	private void randomStage(ArrayList<Integer> neighboursRandNumbers) {
		if(neighboursRandNumbers.isEmpty())
		{
			//System.out.println("id = " + this.ID + ", rand num =" + this.randomNum +"::: neighbors =" + neighboursRandNumbers + "add to MIS = true*********************" ); //debug
			//this node should be in the MIS_set
			MIS_set.add(this);
			MIS_set_ids.add(this.ID);
			thisNodeInMIS = true;
			isActive = false;
			numOfActiveNodes--;
		}
		else {
			Collections.sort(neighboursRandNumbers, Collections.reverseOrder());
			//System.out.println("id = " + this.ID + ", rand num =" + this.randomNum +"::: neighbors =" + neighboursRandNumbers + "add to MIS = " +(randomNum > neighboursRandNumbers.get(0))); //debug
			
			if(randomNum > neighboursRandNumbers.get(0))
			{
				//this node should be in the MIS_set and all the neighbors should become inactive
				MIS_set.add(this);
				MIS_set_ids.add(this.ID);
				thisNodeInMIS = true;
				isActive = false;
				numOfActiveNodes--;
				
				Iterator<Edge> iterator = this.outgoingConnections.iterator();
				iterator.forEachRemaining(edge -> {UdgNetworkNode neighbor = (UdgNetworkNode)edge.endNode;
													if(neighbor.isActive == true) {
													neighbor.isActive = false;
													numOfActiveNodes--;}});
			}
		}
	}
	
	
	/**
	 * get a random number in range of 1 and n^10
	 */
	private void setNewRandomNum() {
		this.randomNum = (int)(Math.random() * (int)(Math.pow(N, 10))) + 1;	
	}

	
	/**
	 * checks if the given node is a neighbor of the current node
	 * @param aNode
	 * @return
	 */
	private boolean isNeighbor(Node aNode) {
		ArrayList<UdgNetworkNode> neighborsList = new ArrayList<UdgNetworkNode>();
		Iterator<Edge> iterator = this.outgoingConnections.iterator();
		iterator.forEachRemaining(edge -> {if(edge!=null) {UdgNetworkNode neighbor = (UdgNetworkNode)edge.endNode;
											neighborsList.add(neighbor);}});
		
		if(neighborsList.contains(aNode)) {
			return true;
		} 
		return false;
	}
	
	
	/**
	 * checks if the given node is a neighbor of the current node
	 * @param aNode
	 * @return
	 */
	private boolean isNeighbor(Integer nodeId) {
		ArrayList<Integer> neighborsIdList = new ArrayList<Integer>();
		Iterator<Edge> iterator = this.outgoingConnections.iterator();
		iterator.forEachRemaining(edge -> {if(edge!=null) {UdgNetworkNode neighbor = (UdgNetworkNode)edge.endNode;
											neighborsIdList.add(neighbor.ID);}});
		
		if(neighborsIdList.contains(nodeId)) {
			return true;
		} 
		return false;
	}
	
	/**
	 * right click event- choose the source, target and a near to the source Mis node to send a message.
	 * after choosing each node its color changes: 
	 * source= orange, target= red, near to the source Mis node= magenta.
	 */
	@NodePopupMethod(menuText = "Send SMS to...")
	public void sendSMS() {
		Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
			
			public void handleNodeSelectedEvent(Node node) {
				if(node == null) {
					return;
				}

				Tools.getNodeSelectedByUser(new NodeSelectionHandler() {
					public void handleNodeSelectedEvent(Node misNode_a) {
						if(misNode_a == null) {
							return;
						}
						misNode_a.setColor(Color.MAGENTA);
						nearMisNodeForSend = (UdgNetworkNode)misNode_a;
						
						String text = JOptionPane.showInputDialog(null, "Please enter the text to send");
						if(text == null) {
							return;
						}
						destinationNode = (UdgNetworkNode)node;
						textMessage = text;
						UdgNetworkNode.this.setColor(Color.RED);
						node.setColor(Color.ORANGE);
						
						SmsTimer timer = new SmsTimer(textMessage, destinationNode, nearMisNodeForSend);
						timer.startRelative(1, UdgNetworkNode.this);
					}
				},"Select a MIS node near the target node.");
			}
		}, "Select a destination node to which the message will be sent to.");
	}
	

	/**
	 * get the message path as an ArrayList<Integer>.
	 * the order is: <target,...,source>
	 * @param destinationNodeId
	 * @param nearMisNodeForSend
	 * @return
	 */
	private ArrayList<Integer> getMsgPath(Integer destinationNodeId, UdgNetworkNode nearMisNodeForSend) {
		boolean finished = false;
		
		if (!nearMisNodeForSend.misNodeRoutingMap.containsKey(destinationNodeId)) {
			//if there is no route between the target and the source
			if(destinationNodeId!=nearMisNodeForSend.ID) {
				//JOptionPane.showMessageDialog(null, "There is no route between these two nodes");
				return null;
			}
		}
		
		ArrayList<Integer> path = new ArrayList<Integer>();
		Integer nextNode = destinationNodeId;
		path.add(destinationNodeId);
		
		while(!finished) {
			Integer fatherId = nearMisNodeForSend.misNodeRoutingMap.get(nextNode);
			path.add(fatherId);
			nextNode = fatherId;
			
			if(fatherId == nearMisNodeForSend.ID) {
				finished = true;
			}
		}
		
		return path;
	}

	
	@Override
	public void init() {}
	
	@Override
	public void neighborhoodChange() {}

	@Override
	public void preStep() {
		roundNum ++;
		
		if(numOfActiveNodes == 0)
			misConstructionFinishRound = roundNum;
		
		//operate random state
		if(roundNum <= tValue){
			setNewRandomNum();
			MisMessage msg = new MisMessage();
			broadcast(msg);
		}
		
		//operate deterministic stage
		else if(numOfActiveNodes > 0 && isActive) {
			deterministicStage();	
		}
		
		//build BFS
		else if(roundNum == misConstructionFinishRound && !isActive && !bfsConstructionDone ) {
			roundInBfsConstruction++;
			//send Bfs request from this MIS nodes
			if(thisNodeInMIS && roundInBfsConstruction == 1) {
				BfsReqMessage msg = new BfsReqMessage();
				msg.setBfsLayerNum(roundInBfsConstruction);
				msg.setMisSenderNodeId(this.ID);
				msg.visitedNods.add(this.ID);
				broadcast(msg);
			}
			if(roundInBfsConstruction > 10) {//2*(int)Math.log(N)) {
				bfsConstructionDone = true;
			}
		}
		else if(bfsConstructionDone && thisNodeInMIS) {
			printDebugStatus();
			
		}
		
	}


	@Override
	public void postStep() {
		
		if(messageSenderNode)
			this.setColor(Color.YELLOW);
		else if(messageDestinationNode)
			this.setColor(Color.ORANGE);
		else if(messageRecieved) {
			//if(MIS_set.contains(this))
			//	this.setColor(Color.PINK);
			//else
			this.setColor(Color.GREEN);}
		else if(MIS_set.contains(this))
			this.setColor(Color.BLUE);
		else
			this.setColor(Color.GRAY);
	}

	public UdgNetworkNode() {
		try {
			this.defaultDrawingSizeInPixels = Configuration.getIntegerParameter("Node/defaultSize");
		} catch (CorruptConfigurationEntryException e) {
			Tools.fatalError(e.getMessage());
		}
		
		{ try {
			radius = Configuration.getIntegerParameter("GeometricNodeCollection/rMax");
		} catch(CorruptConfigurationEntryException e) {
			Tools.fatalError(e.getMessage());
		}}
	}

	
	/**
	 * Helper class to compare two nodes by their ID
	 */
	class NodeComparer implements Comparator<Node> {
		public int compare(Node n1, Node n2) {
			return n1.ID < n2.ID ? -1 : n1.ID == n2.ID ? 0 : 1;   
		}
	}

	public static void set_tValue(int tValueInt) {
		tValue = tValueInt;
	}

	public static void setTotalNumberOfNodes(int totalNumOfNodes) {
		N = totalNumOfNodes;
		numOfActiveNodes = N;
		
	}
	
	public void draw(Graphics g, PositionTransformation pt, boolean highlight){
		Color bckup = g.getColor();
		g.setColor(Color.BLACK);
		this.drawingSizeInPixels = (int) (defaultDrawingSizeInPixels * pt.getZoomFactor());
		super.drawAsDisk(g, pt, highlight, drawingSizeInPixels);
		g.setColor(Color.LIGHT_GRAY);
		pt.translateToGUIPosition(this.getPosition());
		int r = (int) (radius * pt.getZoomFactor());
		g.drawOval(pt.guiX - r, pt.guiY - r, r*2, r*2);
		g.setColor(bckup);
	}

	/**
	 * print debug status- max deg, max routing table size, mis size etc.
	 */
	private void printDebugStatus() {
		System.out.println("\n************************************************************");
		System.out.println("*******************max deg in graph = " + maxDegInGraph + "*********************");
		System.out.println("**************max routing table size = " + maxRoutingTableSize + "********************");
		System.out.println("********************MIS_Size= "+MIS_set.size()+ "***************************");
		System.out.println("************************************************************\n");
		
		System.out.printf("inMissId = %d. num of neighbours = %d. entries = ", this.ID,this.outgoingConnections.size() );//debug
		boolean first = true;
		for (Integer nodeId: misNodeRoutingMap.keySet()){
            Integer value = misNodeRoutingMap.get(nodeId);
            if(!first)
            	System.out.printf("                                                 ");
            else
            	first = false;
    		System.out.printf("father = %d-> son = %d\n",/*it.next().toString()*/value, nodeId );
    		}
    	System.out.printf("\n");
	}

	public boolean isMessageSenderNode() {
		return messageSenderNode;
	}

	public void setMessageSenderNode(boolean messageSenderNode) {
		this.messageSenderNode = messageSenderNode;
	}

	public boolean isMessageDestinationNode() {
		return messageDestinationNode;
	}

	public void setMessageDestinationNode(boolean messageDestinationNode) {
		this.messageDestinationNode = messageDestinationNode;
	}

	public void ClearColors() {
		this.messageDestinationNode = false;
		this.messageSenderNode = false;
		this.messageRecieved = false;
	}

}
