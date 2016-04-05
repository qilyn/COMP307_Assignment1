package part2;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class TreeNode implements Comparable<TreeNode>, Iterable<TreeNode> {
	private final boolean[] myData;
	private final boolean isPure;
	private String condition; // obviously won't be a string
	private Set<Instance> falseCases;
	private Set<Instance> trueCases;
	private int myIndex;

	public TreeNode() {
		isPure = true;
		myData = new boolean[0];
	}

	public TreeNode (boolean[] data) {
		myData = new boolean[data.length];
		boolean purity = true;
		for (int i = 0; i < data.length-1; i++) {
			if (myData[i] != myData[i+1]) {
				purity = false;
			}
			myData[i] = data[i];
			if (i == data.length-2) {
				myData[i+1] = data[i+1];
			}
		}
		isPure = purity;
		System.out.println(data.toString() +" -> "+myData.toString());
	}

	@Override
	public int compareTo(TreeNode o) {
		// TODO Auto-generated method stub
		System.err.println("TreeNode compareTo is not implemented.");
		return 0;
	}

	@Override
	public Iterator<TreeNode> iterator() {
		System.err.println("TreeNode iterator is not implemented. Pretend it is. ");
		return null;
	}
}
