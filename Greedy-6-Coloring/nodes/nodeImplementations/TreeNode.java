/*
Author : Elad Chamilevsky
Id: 204086631
*/
package projects.sample6.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;


import projects.defaultProject.nodes.timers.MessageTimer;
import projects.sample6.nodes.messages.MarkMessage;
import projects.sample6.nodes.messages.ShiftDownMsg;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

/**
 * An internal node (or leaf node) of the tree.
 */
public class TreeNode extends Node {

	public static int globalIdCounter = 0;
	public static boolean shiftDownInProcess = false;
	
	private int roundCounter = 0;
	private int greedyRoundCounter = 6;
	private int nodeColor;
	private boolean isRoot = false;
	private static Color[] colors = {Color.MAGENTA, Color.BLUE, Color.GREEN, 
			 Color.ORANGE , Color.RED, Color.PINK};
	private List<Integer> validColors = new ArrayList<Integer>();
	

	public TreeNode parent = null; // the parent in the tree, null if this node is the root
	private int lastRoundVisited = 0;
	
	public TreeNode() {
		nodeColor = globalIdCounter++;
	}
	
	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}

	@Override
	public void handleMessages(Inbox inbox) {
		while(inbox.hasNext()) {
			Message m = inbox.next();
			if(m instanceof MarkMessage) {
				
				TreeNode sender = (TreeNode) inbox.getSender();
				if(parent != null && !sender.equals(parent)) {
					continue;// don't consider mark messages sent by children
				}
				
				//handle 6 vertex coloring
				if(!isInitialColoringEnded()) {
					if(parent == null) {//the root
						this.isRoot = true;
						this.nodeColor = reduceColorByParentColor(sender.getNodeColor(), true);
					}
					else if(!isInitialColoringEnded()) {
						this.nodeColor = reduceColorByParentColor(sender.getNodeColor(), false);
					}	
				}
				//handle greedy 6 vertex coloring
				else if(greedyRoundCounter>0) {
					if(greedyRoundCounter==6)
						shiftDown(this.isRoot);
					else
						setGreedyFreeColor();
				}
			}
		}
	}

	private void shiftDown(boolean isRoot) {
		if(!isRoot)
		{
			this.setNodeColor(parent.getNodeColor());
		}
		else
		{
			setGreedyFreeColor();
		}
	}

	private int reduceColorByParentColor(int parentColor, boolean isRoot) {
		String thisColorBinary = Integer.toBinaryString(this.nodeColor);
		String parentColorBinary = Integer.toBinaryString(parentColor);
				
		int maxLength = Math.max(thisColorBinary.length(),parentColorBinary.length());
		
		while(thisColorBinary.length() < 3 || thisColorBinary.length()<maxLength) { //too short. make him have at least length 3 and in maxLength	
			thisColorBinary = "0" + thisColorBinary;
		}
		while(parentColorBinary.length() < 3 || parentColorBinary.length()<maxLength) { //too short. make him have at least length 3 and in maxLength				
			parentColorBinary = "0" + parentColorBinary;
		}		

		int k = 0;

		if(isRoot) {
			k = (int )(Math.random() * (thisColorBinary.length() - 1));
		}		
		else {	
			for(int i=0; i < thisColorBinary.length(); i++) {
				if(thisColorBinary.charAt(i) != parentColorBinary.charAt(i)) {
					k=i;
					break;
				}				
			}
		}	
						
		StringBuilder sb = new StringBuilder();
		sb.append(Integer.toBinaryString(k));
		sb.append(thisColorBinary.charAt(k));

		return Integer.parseInt(sb.toString(), 2);
	}

	private void setGreedyFreeColor() {
		if(lastRoundVisited < roundCounter) {
			lastRoundVisited = roundCounter;
		}
		else
			return;
		
		//update the possible valid colors
		for(int i = 0; i<6;i++) {
			if(parent!= null) {
				if(i != parent.getNodeColor())
					validColors.add(i); 
			}
			else
			{
				//if(i != this.getNodeColor())
					validColors.add(i);
			}
		}
	
		List<Integer> neighborsColors = new ArrayList<Integer>();
		
		//iterate over the neighbors of this node and choose their colors greedily
		
		Iterator<Edge> iterator = this.outgoingConnections.iterator();
		iterator.forEachRemaining(edge -> {TreeNode neighbor = (TreeNode)edge.endNode;
											int neighborColor = neighbor.getNodeColor();
											neighborsColors.add(neighborColor);});
		
		for (Integer i : neighborsColors) {
			if(validColors.contains(i))
				validColors.remove(i);
		}
		
		if(!validColors.isEmpty()) {									
			int greedyColor = validColors.get(0);
			this.setNodeColor(greedyColor);
		}
		
		//clear possible valid colors list
		while(!validColors.isEmpty()) {
			validColors.remove(0);	
		}	
	}
	
	public int getNodeColor()
	{
		return this.nodeColor;
	}
	
	public void setNodeColor(int newColor) {
		this.nodeColor = newColor;
	}
	
	@Override
	public void init() {
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void preStep() {
		if(greedyRoundCounter>0) {	
			//we are in the initial coloring phase, 
			//broadcast to the neighbors so that the children can learn this' color
			MarkMessage msg = new MarkMessage();
			MessageTimer timer = new MessageTimer(msg);
			timer.startRelative(1, this);
			roundCounter++;
		}
	}

	@Override
	public void postStep() {
		this.setColor(colors[nodeColor % colors.length]);
		if(isInitialColoringEnded())
			greedyRoundCounter--;
	}
	
	public void draw(Graphics g, PositionTransformation pt, boolean highlight){
		super.drawNodeAsDiskWithText(g, pt, highlight, Integer.toString(this.nodeColor), 15, Color.YELLOW);
	} 
	
	private boolean isInitialColoringEnded() {
		return roundCounter > 6;
	}
	
/*	@NodePopupMethod(menuText = "Color children") 
	public void colorKids() {
		MarkMessage msg = new MarkMessage();
		MessageTimer timer = new MessageTimer(msg);
		timer.startRelative(1, this);
	}*/

}
