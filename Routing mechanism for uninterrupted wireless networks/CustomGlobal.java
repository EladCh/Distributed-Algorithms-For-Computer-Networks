/*
 Copyright (c) 2007, Distributed Computing Group (DCG)
                    ETH Zurich
                    Switzerland
                    dcg.ethz.ch

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

 - Neither the name 'Sinalgo' nor the names of its contributors may be
   used to endorse or promote products derived from this software
   without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.maman15;


import java.awt.Color;

import javax.swing.JOptionPane;

import projects.maman15.nodes.nodeImplementations.UdgNetworkNode;
import sinalgo.nodes.Node;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.tools.Tools;

/**
 * This class holds customized global state and methods for the framework. 
 * The only mandatory method to overwrite is 
 * <code>hasTerminated</code>
 * <br>
 * Optional methods to override are
 * <ul>
 * <li><code>customPaint</code></li>
 * <li><code>handleEmptyEventQueue</code></li>
 * <li><code>onExit</code></li>
 * <li><code>preRun</code></li>
 * <li><code>preRound</code></li>
 * <li><code>postRound</code></li>
 * <li><code>checkProjectRequirements</code></li>
 * </ul>
 * @see sinalgo.runtime.AbstractCustomGlobal for more details.
 * <br>
 * In addition, this class also provides the possibility to extend the framework with
 * custom methods that can be called either through the menu or via a button that is
 * added to the GUI. 
 */
public class CustomGlobal extends AbstractCustomGlobal{
	
	private int totalNumOfNodes;

	/* (non-Javadoc)
	 * @see runtime.AbstractCustomGlobal#hasTerminated()
	 */
	public boolean hasTerminated() {
		return false;
	}

	/**
	 * An example of a method that will be available through the menu of the GUI.
	 */
	@AbstractCustomGlobal.GlobalMethod(menuText="Echo")
	public void echo() {
		// Query the user for an input
		String answer = JOptionPane.showInputDialog(null, "This is an example.\nType in any text to echo.");
		// Show an information message 
		JOptionPane.showMessageDialog(null, "You typed '" + answer + "'", "Example Echo", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * t value button. the user inserts a t value which the algorithm uses to find MIS
	 */
	@AbstractCustomGlobal.CustomButton(buttonText="Enter t parameter", toolTipText="Enter t value for the algorithem use. default sets to 7")
	public void tValueButton() {
		String tValueString = JOptionPane.showInputDialog("t value:");
		int tValueInt = Integer.parseInt(tValueString);
		if(tValueInt > 0) {
			UdgNetworkNode.set_tValue(tValueInt);
			UdgNetworkNode.setTotalNumberOfNodes(totalNumOfNodes);
			JOptionPane.showMessageDialog(null, "t value was set successfully");
		}
		else
			JOptionPane.showMessageDialog(null, "invalid value. t value must be greater than 0");
	}
	
	
	/**
	 * t value button. the user inserts a t value which the algorithm uses to find MIS
	 */
	@AbstractCustomGlobal.CustomButton(buttonText="Clear path colors", toolTipText="Clear the path colors. Mis nodes are colored in blue, other nodes are colored in grey")
	public void clearColorsButton() {
		for(Node n : Tools.getNodeList()) {
			UdgNetworkNode node = (UdgNetworkNode)n;
			node.ClearColors();
		}
	}
	
	
	/**
	 * The framework calls this method whenever a node is added to the
	 * framework. (The method is called after addition.)
	 * @param n The node that was added  
	 */
	@Override
	public void nodeAddedEvent(Node n) {
		totalNumOfNodes++;
	}
	
}
