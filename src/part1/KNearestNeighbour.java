package part1;
import java.util.Vector;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class KNearestNeighbour {
	@SuppressWarnings("unused")
	private final File trainingFile; // file we are using as base
	@SuppressWarnings("unused")
	private final File dataFile; // file we are applying our learning to
	// need some means of storing the data
	// review type of collection later.
	private final HashMap<Vector<Double>,String> myTrainingData = new HashMap<>();
	public final int FIELDS;
	public final int K;

	private final Vector<Double> maxFeatureValues = new Vector<Double>();
	private final Vector<Double> minFeatureValues = new Vector<Double>();

	public KNearestNeighbour(int k, int fields, String learningPath, String applyingPath) {
		trainingFile = new File(learningPath);
		dataFile = new File(applyingPath);
		this.K = k;
		this.FIELDS = fields;
		readTrainingFile();
	}

	/** Reads all information from the paths given when the object was created.	 */
	public void readTrainingFile() {
		// first, read the training data into some storage location
		System.out.println("monolith");
		try {
			Scanner s = new Scanner(trainingFile);
			while (s.hasNext()) {
				Scanner lineScan = new Scanner(s.nextLine());
				double data[] = new double[FIELDS];
				int count = 0;
				// this is an awful, rigid way of reading things, but hopefully that's not what we're being assessed on.
				while (count < FIELDS && lineScan.hasNext()) {
					data[count] = lineScan.nextDouble();
					updateFeatureRanges(count,data[count]);
					count++;
				}
				if (count != FIELDS) {
					System.err.println("... Why are you trying to read something with "+count+" parameters instead of "+FIELDS+". Why would you be so cruel.");
					System.exit(-1);
				}
				String name = lineScan.next();
				Vector<Double> currentRow = new Vector<>();
				for (double d : data) {
					currentRow.add(d);
				}
				myTrainingData.put(currentRow,name);
				//System.out.println(currentRow.toString() +" in "+ name);
				lineScan.close();
			}
			System.out.println("max feature values = "+maxFeatureValues.toString()+"\tmin feature values = "+minFeatureValues.toString());
			s.close();
		} catch (FileNotFoundException e) {
			System.err.println("Training file not found, you should have put tests for this in main.");
			System.err.println("Attempted to read "+trainingFile.getAbsolutePath()+".");
			System.exit(-1);
		}
	}

	/** */
	private void updateFeatureRanges(int count, double d) {
		// first check the minimum values
		if (count == minFeatureValues.size() && minFeatureValues.size() < FIELDS) {
			minFeatureValues.add(d);
		} else if (count < minFeatureValues.size()
				&& d < minFeatureValues.get(count)) {
			minFeatureValues.set(count, d);
		}
		// then check the maximum values
		if (count == maxFeatureValues.size() && maxFeatureValues.size() < FIELDS) {
			maxFeatureValues.add(d);
		} else if (count < maxFeatureValues.size()
				&& d > maxFeatureValues.get(count)) {
			maxFeatureValues.set(count, d);
		}
	}

	/** Reads the data file, line by line, and comparing against the training file. */
	public void sortData() {
		if (myTrainingData.isEmpty()) {
			System.err.println("Oh no! We have no data.");
			System.exit(-1);
		}
		try {
			Scanner s = new Scanner(dataFile);
			while (s.hasNextLine()) {
				Scanner lineScan = new Scanner(s.nextLine());
				Vector v = new Vector();
				int count = 0;
				while (lineScan.hasNext()) {
					// this is necessary if we are testing against training files!
					if (lineScan.hasNext() && count == FIELDS) {
						lineScan.next();
					} else {
						v.addElement(lineScan.nextDouble());
						count++;
					}
				}
				String classification = getClassification(v);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Data file not found, you should have put tests for this in main.");
			System.err.println("Attempted to read "+dataFile.getAbsolutePath()+".");
			System.exit(-1);
		}
	}

	/** Requires: size of unseenVector equals size of all map entries */
	private String getClassification(Vector<Double> unseenVector) {
		String classification;
		// we know we have to get K nearest neighbours
		Entry<Vector<Double>,String>[] bestKValues = new Entry[K];
		double[] bestKDistances = new double[K]; // and recall the distances between them and our new value(s)
		Arrays.fill(bestKDistances, Integer.MAX_VALUE);
		// now we must iterate through our
		Iterator<Entry<Vector<Double>, String>> iter = myTrainingData.entrySet().iterator();

		while (iter.hasNext()) { // now for each element...
			Entry<Vector<Double>,String> currentRow = iter.next();
			double totalSoFar = 0;
			// do the (ai-bi)^2/Ri^2 + ... + (an-bn)^2/Rn^2 part
			for (int i = 0; i < unseenVector.size(); i++) {
				double diff = (unseenVector.get(i) - currentRow.getKey().get(i));
				double range = maxFeatureValues.get(i) - minFeatureValues.get(i);
				totalSoFar += (diff*diff) / (range*range);
				//System.out.println("\tresult += "+(diff*diff) / (range*range));
			}

			double distance = Math.sqrt(totalSoFar); // now we have the distance between this vector pair

			double formerMostDistantDistance = bestKDistances[0];
			int mostDistantIndex = -1;
			for (int i = 0; i < bestKDistances.length; i++) {
				// if they're exactly the same, we expect the label to be the same
				// so,
				if (distance <= bestKDistances[i]
						&& distance <= formerMostDistantDistance) {
					formerMostDistantDistance = bestKDistances[i];
					mostDistantIndex = i;
				}
			}
			if (mostDistantIndex >= 0) {
				System.out.println("["+mostDistantIndex+"]: replacing "+ formerMostDistantDistance
						+ " with a distance of ["+distance+"]"
//						+ ((bestKValues[mostDistantIndex] != null) ? (bestKDistances[mostDistantIndex]+":"+bestKValues[mostDistantIndex].getKey().toString())
//								: "null")
//						+" is replaced by "+distance+":"+currentRow.getKey().toString()
						);
				bestKDistances[mostDistantIndex] = distance;
				bestKValues[mostDistantIndex] = currentRow;
			}
		}
		return null;
	}

	public static void main (String[] input) {
		String test;
		String data;
		if (input.length < 3 || input.length > 3) {
			System.err.println("This program requires two parameters, not "+input.length+". The first is a training file and the second is a data file.");
			//System.exit(-1);
			System.out.println("\n(But we'll just use iris-training and iris-test anyway, because clearly this is a work in progress.)");
			test = "/home/mckayvick/COMP 307/1/part1/iris-training.txt";
			data = "/home/mckayvick/COMP 307/1/part1/iris-test.txt";
		} else {
			test = input[1];
			data = input[2];
		}
		// TODO error checking here

		KNearestNeighbour m = new KNearestNeighbour(3,4,test,data);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m.sortData();
	}
}
