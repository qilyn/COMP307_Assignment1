package part1;
import java.util.Vector;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

public class KNearestNeighbour {
	// TODO: make it write results to a file properly
	// TODO: make it actually classify things

	private String trainingPath = "./data/iris-training.txt"; // file we are using as base
	private String dataPath = "./data/iris-test.txt"; // file we are applying our learning to
	// need some means of storing the data
	// review type of collection later.
	private final HashMap<Vector<Double>,String> myTrainingData = new HashMap<>();
	public final int FIELDS;
	public final int K;

	private final Vector<Double> maxFeatureValues = new Vector<Double>();
	private final Vector<Double> minFeatureValues = new Vector<Double>();

	public KNearestNeighbour(int k, int fields, String trainingPath, String dataPath) {
		this.trainingPath = trainingPath;
		this.dataPath = dataPath;
		this.K = k;
		this.FIELDS = fields;
		readTrainingFile();
	}
	public KNearestNeighbour(int k, int fields) {
		this.K = k;
		this.FIELDS = fields;
		readTrainingFile();
	}

	/** Reads all information from the paths given when the object was created.	 */
	public void readTrainingFile() {
		// first, read the training data into some storage location
		System.out.println("monolith");
		try {
			Scanner s = new Scanner(new File(trainingPath));
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
			System.err.println("Attempted to read "+trainingPath+".");
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
		Date dateFile = new Date(System.currentTimeMillis());
		String name = "D"+dateFile.getDay()+"-M"+dateFile.getMonth()
			+"-at-"+dateFile.getHours()+"-"+dateFile.getMinutes()+"-"+dateFile.getSeconds()+".txt";
		if (myTrainingData.isEmpty()) {
			System.err.println("Oh no! We have no data.");
			System.exit(-1);
		}
		try {
			System.out.println("Path");
			try {
				Files.createDirectory(Paths.get("results"));
			} catch (FileAlreadyExistsException e) {

			}
			Scanner s = new Scanner(new File(dataPath));
			while (s.hasNextLine()) {
				Scanner lineScan = new Scanner(s.nextLine());
				Vector<Double> v = new Vector<Double>();
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
				String lineOut = "";
				for (Double d : v) {
					lineOut += d +"\t";
				}
				lineOut += getClassification(v) +"\n";
				try {
					BufferedWriter writer = Files.newBufferedWriter(Paths.get("results/"+name),Charset.forName("US-ASCII"));
						writer.write(lineOut);
				} catch (IOException x) {
					System.err.println("IOException.");
				}
				lineScan.close();
			}
			s.close();
		} catch (FileNotFoundException e) {
			System.err.println("Data file not found, you should have put tests for this in main.");
			System.err.println("Attempted to read "+dataPath+".");
			System.exit(-1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** Requires: size of unseenVector equals size of all map entries */
	private String getClassification(Vector<Double> unseenVector) {
		String classification = "";
		// we know we have to get K nearest neighbours
		@SuppressWarnings("unchecked")
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
			if (mostDistantIndex > 0 && distance != 0) {
//				System.out.println("["+mostDistantIndex+"]: replacing "+ formerMostDistantDistance
//						+ " with a distance of "+distance+"."
//						+ ((bestKValues[mostDistantIndex] != null) ? (bestKDistances[mostDistantIndex]+":"+bestKValues[mostDistantIndex].getKey().toString())
//								: "null")
//						+" is replaced by "+distance+":"+currentRow.getKey().toString()
//						);
				bestKDistances[mostDistantIndex] = distance;
				bestKValues[mostDistantIndex] = currentRow;
			}
		}

		if (K == 1) {
			return bestKValues[0].getValue();
		}

		// now we find the best classification
		// this seems like horrific overkill unless you want k = 100 or something
		Entry<Vector<Double>,String> mostCommon = bestKValues[0];
		int[] countOfClassifier = new int[bestKValues.length];
		int[] indexOfClassifier = new int[bestKValues.length];
		Arrays.fill(indexOfClassifier, -1);
		for (int i = 0; i < bestKValues.length; i++) {
			for (int j = 0; j <= i; j++) {
				if (bestKValues[i].equals(bestKValues[j].getValue())) {
					if (j < i && indexOfClassifier[j] < 1) {
						indexOfClassifier[i] = j;
					}
					countOfClassifier[j]++;
				}

			}
			System.out.println("you can find "+bestKValues[i].getValue()+" at index "+indexOfClassifier[i]);
		}
		return classification;
	}

	public static void main (String[] input) {
		String test;
		String data;
		int k = 3;
		int fields = 4;
		if (input.length < 3 || input.length > 3) {
			System.err.println("This program requires two parameters, not "+input.length+". The first is a training file and the second is a data file.");
			//System.exit(-1);
			System.out.println("\n(But we'll use iris-training and iris-test by default.)");
			test = "./data/iris-training.txt";
			data = "./iris-test.txt";
		} else {
			test = input[1];
			data = input[2];
		}
		// TODO error checking here

		KNearestNeighbour m = new KNearestNeighbour(k,fields,test,data);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m.sortData();
	}
}
