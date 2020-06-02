# Operating Instructions:

1. Creating the Network:
- Click the Add Nodes button.
- Enter the number of vertices in the Number of Nodes field.
- Press OK.
- Click the Reevaluate Connectiond button to link the vertices according to UDG model requests.

2. Calculate maximum independent set
- Click the Enter t parameter button.
- Enter a numeric value greater than 0. The value is a barrier to the randomization phase of the MIS build run.
- Run the simulator as t + 3 turns (there are additional turns following the sinalgo simulation).
- vertices joining the MIS will be blue.

3. Calculate BFS from all MIS vertices
- Run the O(Diam (G)) rotation algorithm. All the runs are done in parallel.

4. Message routing
- Right clicking on a vertex with the will open a menu to choose from: send SMS to.
- Follow the instructions written in red on the upper left of the screen.
(Source selection, destination, MIS vertex near source and message connection).
- Run the simulation step by step to see the message passage.
Source vertex is yellow, intermediate vertex is green and target vertex is orange.
In the event of a destination or failure to find a route, a pop-up message will appear.

# Complexity

## Calculate Maximum Independent Set:
### Random Step:
At the beginning of each round, the preStep function arises, which has the counter of the number of turns.
During the first rounds, a random number is increased according to the instruction in the exercise and is kept in an internal member.
MisMessage messages are also sent to all neighbors for the purpose of calculating the independent set.
In the handleMasseges function, if the appropriate conditions go beyond all incoming messages and entering the neighbors' random numbers into the array.
Once all messages have arrived, the array is sorted so that the largest value is in the first cell of the array, and we compare it to the random number of the vertex that runs the code.
If the value of the invariant vertex is greater than any of its neighbors it is added to the MIS, it becomes inactive.
In addition, the vertex goes over its neighbors list and renders them inactive (too, more efficient than sending a message to be done in the next cycle).
If a neighboring vertex has no neighbor, it is added to the MIS itself and becomes inactive. Total run-time t rounds.

### Deterministic phase:
After t iterations of the random phase, the deterministic phase is performed.
The phase takes one round. Because there are single vertices with no neighbors the execution of the phase as a response to the message will not do its job, so I chose to put the implementation of the phase in the preStep function.
There are two conditions for running the phase: 1. There are active vertices. 2. The current vertex is still active.
The phase run is performed by calling the deterministicStage function.
At the beginning of the function, check if all neighbors with larger identification numbers are finished (inactive).
If there is a neighbor with a larger identification number that is already a member of the MIS-vertex does not enter the MIS and becomes inactive.
Otherwise, the vertex enters the MIS and becomes inactive. Total Run- O(num of active nodes) rounds.

## BFS
Once the maximum independent set has been calculated, at the same time, each vertex in the set will send a broadcast BfsReqMessage message to its neighbors.
When a vertex receives the message, it checks to see if the message has reached it previously by the msg.visitedNodes field.
If this is not the first time - the vertex ends. Otherwise, the vertex adds itself to the list.

Each node has a routing table implemented by map <MisRootId, routingMap> where routingMap is implemented by map <nodeID, nodeFather>.
That is, each vertex has a table where, given the MIS vertex, a record is obtained with which you can get a pointer to the vertex's father by a unique identifier.
If there is a record in the general routing table for the initiating MIS vertex - we will update the internal record.
Otherwise we will create a record in the general routing table and add the internal record to it.
Each vertex checks which of its neighbors has not yet received the message and sends the message to them (while updating parameters within the message).
Each time arriving to a new vertex, the vertex defaults to the message, adds its details, its ID to the solution's path identifier, and returns the message to its father (according to each vertex routing table) until the message arrives to the initiator (each vertex recipient of the message adds itself to the solution path list).
When the message arrives, the path sent in the message to the entries in the routing table is translated.
The initiator now has information about routing all vertices in the message path.
Additionally, the vertex tries to spread the message to its neighbors.
Total run-time O (Diam (G)) rounds

## Routing
After calculating BFS paths from all MIS vertices, you can request a message between vertices on the network.
Clicking on a vertex with the right button opens a menu where you can choose to send a message.
The function is executed under the sendSMS method, which selects source vertex, destination vertex, vertex in the MIS adjacent to the source and connects a message.
After the election is over, a timer is scheduled for the next round, where a message from the source is sent to the adjacent vertex in the MIS.
A path is searched from the vertex in the MIS to the destination. If there is a route, it updates the message and sends the message to the next vertex in the queue.
When the message reaches its destination, a success message appears. If there is no path between the vertices, a failure message will appear (this is possible because, according to the exercise definition, if there is a rout between two vertices, it will be detected in the bfs build, and if no path is detected in the bfs there is no routing.
The route, since if there is routing from the neighboring vertex in the MIS to the destination there is also a route from the original selected vertex).
