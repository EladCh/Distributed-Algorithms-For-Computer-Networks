# Gridy 6-coloring

## The relevant files:
- CustomGlobal: Creating the tree.
- TreeNode: Before running in any vertex, the algorithm.
- MarkMessage: Notation Style Marking for Tree Coloring in 6 Colors (Both Normal and Grid).

# How to use:
- There is a tree connection as in Example 6 of sinalgo.
- The simulator has to run 7 moves for 6 colors and 6 more moves (12 moves total) for 6 colors grids.

# Algorithm:
The next 7 rounds are done in 6 colors according to the cole vishkin algorithm.
Note that the root chooses color randomly (because of the compared bit). You can also see that the vote is valid and not strict.
In the eighth round, a shift down is performed to prevent coloring damage due to two different boys.
From the ninth round to the last round, the grid coloring is done as follows:
Each vertex looks at the colors of its neighbors filtering those colors from the list of allowed colors and choosing the lowest color.
You can see that at the end of the eighth round the numbers (colors) of the vertices are arranged in a valid grid but the colors do not match.
The ninth round arranges the colors.

# Total run time:
For coloring in 6 colors by cole vishkin - we get O (log * n) and for the conversion to grid coloring we get fixed time. Therefore the total run time is O (log * n).