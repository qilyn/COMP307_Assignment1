package part2;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class DecisionLearningTree {
	private final List<Instance> instances = new ArrayList<Instance>(); // instances in our training data FIXME doesn't need to be a field
	private List<Instance> data; // contains the instances to be classified

	private final String trainingFile;
	private final String testFile;
	private List<String> attributeNames = new ArrayList<>();
	private List<String> classifierNames;

	private TreeNode defaultNode = null;
	private TreeNode root; // once calculated, a tree should remember its root node.

	/** Initialises a default decision tree over the golf training files.*/
	public DecisionLearningTree() {
		trainingFile = "./data/golf-training.dat";
		testFile = "./data/golf-test.dat";
	}

	/** Initialises a decision tree from the given training and test file URIs. */
	public DecisionLearningTree(String training, String test) {
		trainingFile = training;
		testFile = test;
	}

	/** First method for setting up the program. This function is called to fills in the
	 * attributes and instances for building the decision learning tree.
	 */
	public void readTrainingFile() {
		// first, read the training data into some storage location
		System.err.println("First, we read the training file...");
		try {
			Scanner s = new Scanner(new File(trainingFile));
			// took these lines from helper-code
		      classifierNames = new ArrayList<String>();
		      for (Scanner l = new Scanner(s.nextLine()); l.hasNext();) {
		    	  classifierNames.add(l.next());
		      }
		      System.out.println(classifierNames.size() +" categories, "+classifierNames.get(0)+" and "+
		    	  classifierNames.get(1));

			if (attributeNames.size() == 0) {
				Scanner line = new Scanner(s.nextLine());
				while (line.hasNext()) {
					attributeNames.add(line.next());
				}
				System.out.println("Read attributes "+attributeNames.toString()+".");
				line.close();
			}
			readFile(s,instances);
			s.close();
		} catch (FileNotFoundException e) {
			System.err.println("Training file not found, you should have put tests for this in main.");
			System.err.println("Attempted to read "+trainingFile+".");
			System.exit(-1);
		}
		System.err.println("Done reading training file.");
		setDefaultNode();
		ArrayList<Boolean> ar = new ArrayList<Boolean>();
		for (int i = 0; i < attributeNames.size(); i++) {
			ar.add(false);
		}
		root = BuildTree(instances, ar, 0);
	}

	/** The second method to be called, which reads the test data instances.
	 * @return
	 */
	public TreeNode readDataFile() {
		data = new ArrayList<Instance>();
		System.err.println("Reading data file.");
		try {
			Scanner s = new Scanner(new File(testFile));
			// took these lines from helper-code
			Scanner l = new Scanner(s.nextLine());
			for (int i = 0; l.hasNext(); i++) {
				if (!l.next().equals(classifierNames.get(i))) {
					System.err.println("This data file does not use the same category names as the training file.");
					break;
				}
			}
			l.close();
			l = new Scanner(s.nextLine());
			for (int i = 0; l.hasNext(); i++) {
				if (!l.next().equals(attributeNames.get(i))) {
					System.err.println("This data file does not match the attributes of the training file.");
					break;
				}
			}
			l.close();
			System.out.println("Read attributes "+attributeNames.toString()+".");
			readFile(s,data);
			s.close();
		} catch (FileNotFoundException e) {
			System.err.println("Training file not found, you should have put tests for this in main.");
			System.err.println("Attempted to read "+testFile+".");
			System.exit(-1);
		}
		ArrayList<Boolean> attrs = new ArrayList<Boolean>();
		for (int i = 0; i < attributeNames.size(); i++) {
			attrs.add(false);
		}
		TreeNode hi = BuildTree(instances, attrs, 0);
		System.err.println("Finished reading data file.");
		return hi;
	}

	/** Helper function, which continues to read the scanner contents
	 * into the destination array.
	 * @param s
	 * @param dest
	 * @throws FileNotFoundException
	 */
	private void readFile(Scanner s, List<Instance> dest) throws FileNotFoundException {
		// first, read the training data into some storage location
		System.err.println("Reading file.");
		while (s.hasNextLine()) {
			Scanner line = new Scanner(s.nextLine());
			if (!line.hasNext()) {
				break;
			}
			String result = line.next();
			boolean[] attrs = new boolean[attributeNames.size()];
			for (int i = 0; line.hasNext(); i++) {
				attrs[i] = line.nextBoolean();
			}
			// print
//			System.out.print("Example "+result+" with ");
//			for (int j = 0; j < attrs.length; j++) {
//				System.out.print(attrs[j] +"\t");
//			}
//			System.out.print("\n");
			// endprint
			Instance a = new Instance(result,attrs);
			dest.add(a); // finally, add our example
			line.close();
		}
		System.err.println("Finished reading file.");
	}

	/** This method sets the default node, to be returned when no more
	 * instances can be found that match the remaining attributes. */
	private void setDefaultNode() {
		System.err.print("Setting default node: ");
		int[] mostCommon = getMostCommonClassAndTotal(this.instances);
		defaultNode = new LeafNode( ((double)mostCommon[1]/(double)instances.size()),
				attributeNames.get(mostCommon[0]));
		System.out.println(defaultNode.toString());
		System.out.println();
	}

	/**
	 * @param instances
	 * @return A 2d array, where index 0 is the index of the most common class name in
	 * classifierNames and index 1 is the number of instances of that class.
	 */
	private int[] getMostCommonClassAndTotal(Iterable<Instance> instances) {
		int[] classes = new int[classifierNames.size()];
		for (Instance i : instances) {
			classes[i.getCategory()]++;
		}
		int largestIndex = 0;
		for (int i = 0; i < classes.length; i++) {
			if (classes[i] > classes[largestIndex]) {
				largestIndex = i;
			} else if (classes[i] == classes[largestIndex]) {
				largestIndex = (Math.random() > 0.5?largestIndex:i); // I hope this is an OK implementation of the randomness
				// I know it's not a perfectly even chance if we have more than a 2-way tie,
				// but in all the examples, we've only had binary classification!
			}
		}
		int[] back = {largestIndex,classes[largestIndex]};
		return back;
	}

	/** Builds a wonderful, splendiferous tree! Must be called after readDataFile()
	 * and readTrainingFile().
	 *
	 * @param instances A List of instances to be used to build the tree.
	 * @param attributes A List with size() == instances.get(0).getAttributeLength(),
	 * 		and where the booleans are only true if the attribute has been used.
	 * @return the head of the tree.
	 */
	private TreeNode BuildTree(List<Instance> instances, List<Boolean> attributes,int depth) {
		if (depth > 50) {// FIXME this is just debug code
			throw new IllegalStateException();
		}
		System.err.println("Building tree node "+depth+"!");
		// if instances is empty
		if (instances.isEmpty()) {
			return defaultNode;
		}
		// if instances are pure
		if (isPure(instances)) {
			LeafNode returnVal = new LeafNode(1,classifierNames.get(instances.get(0).category));
			return returnVal;
			// return leaf node containing CLASS NAME and probability 1
		}
		if (!attributes.contains(false)) { // that is, all attributes have been used
			int[] most = getMostCommonClassAndTotal(instances);
			return new LeafNode((double)((double)instances.size() - (double)most[1]) / (double)instances.size(),
					classifierNames.get(most[0]));
			// return a leaf node containing the name & probability of the MAJORITY CLASS
			// of instances in the node (or choose randomly if equal)
		}
		// else find best attribute
		else {
			int bestAttrIndex = 0;
			double bestPurity = 0;
			List<Instance> bestInstsTrue = new ArrayList<Instance>();
			List<Instance> bestInstsFalse = new ArrayList<Instance>();
			for (int i = 0; i < attributes.size(); i++) {
				// for each remaining attribute,
				if (attributes.get(i)) { // if we have used this attribute, skip!
					continue;
				}
				List<Instance> trueSet = new ArrayList<Instance>();
				List<Instance> falseSet = new ArrayList<Instance>();
				for (Instance inst : instances) {
					if (inst.getAtt(i)) {
						trueSet.add(inst);
					} else {
						falseSet.add(inst);
					}
				}
				double truePurity = purity(trueSet);
				double falsePurity = purity(falseSet);
				double weightedPurity = ((double)trueSet.size()/(double)instances.size()*truePurity)
						+ ((double)falseSet.size()/(double)instances.size()*falsePurity);
				if (weightedPurity > bestPurity) {
					bestAttrIndex = i;
					bestPurity = weightedPurity;
					bestInstsTrue = trueSet;
					bestInstsFalse = falseSet;
				}
			}
			attributes.set(bestAttrIndex,true);
			ArrayList<Boolean> leftArr  = new ArrayList<Boolean>();
			ArrayList<Boolean> rightArr = new ArrayList<Boolean>();
			leftArr.addAll(attributes);
			rightArr.addAll(attributes);
			TreeNode left = BuildTree(bestInstsTrue, leftArr, depth+1);
			TreeNode right = BuildTree(bestInstsFalse, rightArr, depth+2);
			TreeNode me = new Node(attributeNames.get(bestAttrIndex),left,right);
			return me;
		}
	}

	/** The final method! The end of all things!
	 * That is, the bit that classifies the data. Woohoo.
	 * @return
	 */
	public List<String> useTreeOnData() {
		List<String> classifierWithData = new ArrayList<String>();
		if (root == null || data == null || attributeNames.size() == 0) {
			throw new IllegalStateException();
		}
		for (Instance inst : data) {
			String s = root.classify(inst) +" "+ inst.toString();
			classifierWithData.add(s);
		}
		return classifierWithData;
	}

	/** calculates the purity of a given node, represented as a set of instances. */
	public double purity(Iterable<Instance> set) {
		int size = 0;
		// TODO this assumes that we have only true/false classification
		Set<Instance> successNodes = new HashSet<Instance>();
		Set<Instance> failureNodes = new HashSet<Instance>();
		for (Instance i : set) {
			if (i.getCategory() == 0) {
				successNodes.add(i);
			} else {
				failureNodes.add(i);
			}
			size++;
		}
		if (successNodes.size() == 0 || failureNodes.size() == 0) {
			return 1;
		}
		double papb =
				((double)successNodes.size() / (double)size)
				* ((double)failureNodes.size() / (double)size);
		return papb;
	}

	/** Checks if a collection of instances is pure. */
	private boolean isPure(Collection<Instance> instances) {
		if (instances.size() <= 1) {
			return true;
		}
		Iterator<Instance> iter = instances.iterator();
		int match = iter.next().getCategory();
		while (iter.hasNext()) {
			if (match != iter.next().getCategory()) {
				return false;
			}
		}
		return true;
	}




	/**
	 *
	 *
	 * 		MAIN
	 *
	 *
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		DecisionLearningTree dt = null;
		if (args.length == 2)
			dt = new DecisionLearningTree(args[0],args[1]);
		else
			dt = new DecisionLearningTree();
		dt.readTrainingFile();
		System.out.flush();
		System.err.flush();
		System.out.println("=======================================");
		TreeNode d = dt.readDataFile();
		System.out.flush();
		System.err.flush();
		System.out.println("=======================================");
		System.err.println("Printing tree.");
		((Node)d).report("");
		System.out.flush();
		System.err.flush();
		System.out.println("=======================================");
//		System.err.println("Printing each node.");
//		List<String> s = dt.useTreeOnData();
//		for (String st : s)
//			System.out.println(st);
	}







	/** Instances maintain the boolean values of various attributes and the
	 * resulting class name that was assigned.
	 */
	private class Instance {
	    private int category = -1;
	    private boolean[] vals;

	    public Instance(String name, boolean[] s){
	      for (int i=0; i<classifierNames.size();i++) {
	    	  if (name.equals(classifierNames.get(i))) {
	    		  category = i;
	    		  break;
	    	  }
	      }
	      vals = Arrays.copyOf(s, s.length);
	    }

	    public boolean getAtt(int index){
	      return vals[index];
	    }

	    public int getCategory(){
	      return category;
	    }

	    @SuppressWarnings("unused")
		public int getAttributeLength() {
	    	return vals.length;
	    }

	    public String toString(){
	      StringBuilder ans = new StringBuilder(classifierNames.get(category));
	      ans.append(" ");
	      for (Boolean val : vals)
	    	  ans.append(val?"true  ":"false ");
	      return ans.toString();
	    }

	  }







	/** Generic implementation of a TreeNode. */
	public abstract class TreeNode {
		/** A means of printing the full tree recursively. */
		public abstract void report(String string);
		/** Provides the number of nodes in the tree. */
		abstract int size();
		/** A debug method which returns the probability of the left half of the tree.*/
		abstract double leftProbability();
		/** A debug method which returns the probability of the right half of the tree.*/
		abstract double rightProbability();
		/** A recursive method which attempts to classify an instance. */
		abstract String classify(Instance inst);
	}

	/** A non-leaf node. */
	public class Node extends TreeNode {
		public final TreeNode leftNode;
		public final TreeNode rightNode;
		private String className;

		public Node (String className, TreeNode left, TreeNode right) {
			this.className = className;
			leftNode = left;
			rightNode = right;
		}

		String classify(Instance inst) {
			if (attributeNames.size() != inst.getAttributeLength()) {
				throw new IllegalStateException("Cannot classify an instance with a dissimilar number of attributes.");
			}
			int n = attributeNames.indexOf(className);
//			System.out.println(className +" @ "+n+" leads to "+inst.getAtt(n));
			if (inst.getAtt(n)) {
				return (leftNode != null? leftNode.classify(inst) : "not found");
			} else {
				return (rightNode != null? rightNode.classify(inst) : "not found");
			}
		}

		double leftProbability() {
			return (leftNode != null?
					leftNode.leftProbability()*leftNode.rightProbability() : 0);
		}

		double rightProbability() {
			return (rightNode != null?
					rightNode.leftProbability()*rightNode.rightProbability() : 0);
		}

		public int size() {
			return (leftNode != null ? leftNode.size() : 0)
					+ (rightNode !=null ? rightNode.size() : 0) + 1;
		}

		public String toString() {
			return className +" = {L:"+ (leftNode!=null) +" R:"+ (rightNode!=null) +"}";
		}

		public void report(String indent){
			System.out.format("%s%s = True:\n",
				indent, className);
				leftNode.report(indent+" ");
				System.out.format("%s%s = False:\n",
				indent, className);
				rightNode.report(indent+" ");
		}
	}

	/** A leaf node. */
	public class LeafNode extends TreeNode {
		public final double probability;
		public final String result;

		public LeafNode(double p, String r) {
			probability = p;
			result = r;
		}

		public int size() {
			return 1;
		}

		public String toString() {
			return result +" = P"+probability;
		}

		@Override
		public void report(String indent) {
			if (result=="" || result==null)
				System.out.format("%sUnknown\n", indent);
			else {
				System.out.format("%sClass %s, prob=%4.2f\n",indent, result, probability);
			}
		}

		@Override
		double leftProbability() {
			return probability;
		}

		@Override
		double rightProbability() {
			return probability;
		}

		@Override
		String classify(Instance inst) {
			return result;
		}
	}
}