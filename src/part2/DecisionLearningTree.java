package part2;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class DecisionLearningTree {
	private String trainingFile = "./golf-training.dat";
	private String testFile = "./golf-test.dat";

	private String trueName = "";
	private String falseName = "";
	ArrayList<String> attributes = new ArrayList<>();

	private final Set<Example> EXAMPLES = new HashSet<Example>();

	private Example defaultNode = new Example(new boolean[3],false);

	public DecisionLearningTree() {

	}

	public DecisionLearningTree(String training, String test) {
		trainingFile = training;
		testFile = test;
	}

	public void readTrainingFile() {
		// first, read the training data into some storage location
		System.err.println("Reading training file.");
		try {
			Scanner s = new Scanner(new File(trainingFile));

			while (s.hasNextLine()) {
				if (trueName.length() == 0) {
					Scanner line = new Scanner(s.nextLine());
					trueName = line.next();
					falseName = line.next();
					line.close();
				} else if (attributes.size() == 0) {
					Scanner line = new Scanner(s.nextLine());
					while (line.hasNext()) {
						attributes.add(line.next());
					}
					line.close();
				} else {
					Scanner line = new Scanner(s.nextLine());
					if (!line.hasNext()) {
						break;
					}
					String result = line.next();
					boolean[] attrs = new boolean[attributes.size()];
					int i = 0;
					while (line.hasNext()) {
						attrs[i] = line.nextBoolean();
						i++;
					}
					System.err.println("Example "+result+" with "+attrs.toString());
					EXAMPLES.add(new Example(attrs,(result.equalsIgnoreCase(trueName)?true:false)));
					line.close();
				}
			}

			s.close();
		} catch (FileNotFoundException e) {
			System.err.println("Training file not found, you should have put tests for this in main.");
			System.err.println("Attempted to read "+trainingFile+".");
			System.exit(-1);
		}
	}

//	public Example BuildTree(List<Example> instances, List attributes) {
//		// if instances is empty
//		if (instances.isEmpty()) {
//			return defaultNode;
//		}
//		// if instances are pure
//		if (isPure(instances)) {
//			return instances.get(0);
//		}
//		if (attributes.size() == 0) {
//			// return a leaf node containing the name & probability of the MAJORITY CLASS
//			// of instances in the node (or choose randomly if equal)
//		}
//		// else find best attribute
//
//	}

	public double purity(Set<Example> set) {
		Iterator<Example> iter = set.iterator();
		Set<Example> trueNodes = new HashSet<Example>();
		Set<Example> falseNodes = new HashSet<Example>();
		while (iter.hasNext()) {
			Example current = iter.next();
			if (current.result()) {
				trueNodes.add(current);
			} else {
				falseNodes.add(current);
			}
		}
		if (trueNodes.size() == 0 || falseNodes.size() == 0) {
			return 1;
		}
		// if the node isn't false,
		// TODO formula i don't remember help
		// mn / (m + n)^2
		// P(A)P(B)
		double papb = (trueNodes.size()*falseNodes.size())
				/ ((trueNodes.size() + falseNodes.size()) * (trueNodes.size() + falseNodes.size()));
		System.out.println(papb);
		return papb;
	}

	public double weightedProbability(Set<Example> currentSet, int testAttribute) {
		System.err.println("weighted probability");
		Iterator<Example> iter = currentSet.iterator();
		Set<Example> trueNodes = new HashSet<Example>();
		Set<Example> falseNodes = new HashSet<Example>();
		while (iter.hasNext()) {
			Example current = iter.next();
			if (current.result()) {
				trueNodes.add(current);
			} else {
				falseNodes.add(current);
			}
		}
		if (trueNodes.size() == 0 || falseNodes.size() == 0) {
			return 1;
		}
		// number of examples which result in a true result which are also true for the testAttribute
//		int trueTrueExamples = numberOfTrueAttributes(trueNodes, testAttribute);
//		int trueFalseExamples = numberOfTrueAttributes(falseNodes, testAttribute);

//		double papb = (trueNodes.size() / currentSet.size()) * ((trueNodes.size() - trueTrueExamples)/trueNodes.size())
//				+ (falseNodes.size() / currentSet.size()) * ((falseNodes.size() - trueFalseExamples)/falseNodes.size());

//		System.out.println(papb);
//		return papb;
		return 0;
	}

//	private int numberOfTrueAttributes(Set<Example> nodes, int testAttribute) {
//		Iterator<Example> iter = nodes.iterator();
//		int count = 0;
//		while (iter.hasNext()) {
//			if (iter.next().getAttributes().get(testAttribute)) {
//				count++;
//			}
//		}
//		return count;
//	}

	private boolean isPure(List<Example> instances) {
		if (instances.size() <= 1) {
			return true;
		}
		Iterator<Example> iter = instances.iterator();
		boolean match = iter.next().result();
		while (iter.hasNext()) {
			if (match != iter.next().result()) {
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {

	}
}
