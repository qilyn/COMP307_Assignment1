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
	public final int FIELDS;
	public final int K;
	
	private final HashMap<Vector<Double>,String> trainingData = new HashMap<>();
	private final Vector<Double> maxFeatureValues = new Vector<Double>();
	private final Vector<Double> minFeatureValues = new Vector<Double>();
	
	private final String trainingPath; // file we are using as base
	private final String dataPath; // file we are applying our learning to

	public static void main (String[] input) {
		if (input.length != 2) {
			System.err.println("This program requires two parameters, not "+input.length+". The first is a training file and the second is a data file.");
			System.exit(-1);
		}
		int k = 3;
		int fields = 4;
		KNearestNeighbour m;
		m = new KNearestNeighbour(k,fields,input[0],input[1]);
		m.printData();
	}

	public KNearestNeighbour(int k, int fields, String trainingPath, String dataPath) {
		this.trainingPath = trainingPath;
		this.dataPath = dataPath;
		this.K = k;
		this.FIELDS = fields;
		readTrainingFile();
	}

	/** Reads all information from the paths given when the object was created, populating myTrainingData.	 */
	public void readTrainingFile() {
		// first, read the training data into some storage location
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
				trainingData.put(currentRow,name);
				//System.out.println(currentRow.toString() +" in "+ name);
				lineScan.close();
			}
			System.out.println("max feature values = "+maxFeatureValues.toString()+"\tmin feature values = "+minFeatureValues.toString());
			s.close();
		} catch (FileNotFoundException e) {
			System.err.println("Attempted to read "+trainingPath+", but was not found.");
			System.exit(-1);
		}
	}

	/** One of the first called methods which simply updates the max and min feature arrays. */
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

	/** Once the training data has been read and the max/min features accounted for, this method
	 * will read and sort the data in the requested file.  
	 */
	public void printData() {
		if (trainingData.isEmpty()) {
			System.err.println("Oh no! We have no data.");
			System.exit(-1);
		}
		try {
			// now let's read our new data
			Scanner s = new Scanner(new File(dataPath));
			while (s.hasNextLine()) {
				Scanner lineScan = new Scanner(s.nextLine());
				Vector<Double> v = new Vector<Double>();
				int count = 0;
				lineScan.useDelimiter("(\\s|,)+");
				while (lineScan.hasNext()) {
					if (lineScan.hasNext() && count == FIELDS) {
						// escape any given classname
						lineScan.next();
					} else {
						// otherwise add this element to the current vector
						v.addElement(lineScan.nextDouble());
						count++;
					}
				}
				if (count == 0) {
					continue;
				}
				String lineOut = "";
				for (Double d : v) {
					lineOut += d +"   ";
				}
				String classed = getClassification(v);
				lineOut = classed + (classed.length() > 11 ? "\t\t":"   \t\t") + lineOut;
				System.out.println(lineOut);
				lineScan.close();
			}
			s.close();
		} catch (FileNotFoundException e) {
			System.err.println("Data file "+dataPath+" not found.");
			System.exit(-1);
		}
	}
	
	/** Reads the data file, line by line, and comparing against the training file. */
	@Deprecated
	public void sortData() {
		Date dateFile = new Date(System.currentTimeMillis());
		String name = "D"+dateFile.getDay()+"-M"+dateFile.getMonth()
			+"-at-"+dateFile.getHours()+"-"+dateFile.getMinutes()+"-"+dateFile.getSeconds()+".txt";
		if (trainingData.isEmpty()) {
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

	/** Using the populated trainingData array, classifies a given vector.
	 * Requires: size of unseenVector equals size of all map entries */
	private String getClassification(Vector<Double> unseenVector) {
		@SuppressWarnings("unchecked")
		Entry<Vector<Double>,String>[] bestKValues = new Entry[K]; // we know we have to get K nearest neighbours
		double[] bestKDistances = new double[K]; // and recall the distances between them and our new value(s)
		Arrays.fill(bestKDistances, Integer.MAX_VALUE);
		// now we must iterate through them!
		Iterator<Entry<Vector<Double>, String>> iter = trainingData.entrySet().iterator();

		// first we have to find the distance between each element in the training data and
		// the given element. 
		while (iter.hasNext()) { // for each training element...
			Entry<Vector<Double>,String> currentRow = iter.next();
			double totalSoFar = 0;
			// do the (ai-bi)^2/Ri^2 + ... + (an-bn)^2/Rn^2 part
			for (int i = 0; i < unseenVector.size(); i++) {
				double diff = (unseenVector.get(i) - currentRow.getKey().get(i));
				double range = maxFeatureValues.get(i) - minFeatureValues.get(i);
				totalSoFar += (diff*diff) / (range*range);
			}
			double distance = Math.sqrt(totalSoFar); 
			// now we have the distance between these two vectors, the currentRow and the unseenVector
			// is this better than any of our current k distances?
			double formerMostDistantDistance = bestKDistances[0];
			int mostDistantIndex = -1;
			for (int i = 0; i < bestKDistances.length; i++) {
				// if they're exactly the same, we expect the label to be the same
				if (distance <= bestKDistances[i] && distance <= formerMostDistantDistance) {
					formerMostDistantDistance = bestKDistances[i];
					mostDistantIndex = i;
				}
			}
			// if, then, we have found a smaller distance than one of our existing k values,
			if (mostDistantIndex >= 0 && distance != 0) {
//				System.out.println("["+mostDistantIndex+"]: replacing "+ formerMostDistantDistance
//						+ " with a distance of "+distance+"."
//						+ ((bestKValues[mostDistantIndex] != null) ? (bestKDistances[mostDistantIndex]+":"+bestKValues[mostDistantIndex].getKey().toString())
//								: "null")
//						+" is replaced by "+distance+":"+currentRow.getKey().toString()
//						);
				bestKDistances[mostDistantIndex] = distance;
				bestKValues[mostDistantIndex] = currentRow;
			}
		} // ends trainingData loop

		if (K == 1) {
			return bestKValues[0].getValue();
		}

		// now we find the most common class name
		HashMap<String,Integer> classNameToTotal = new HashMap<>();
		for (int i = 0; i < bestKValues.length; i++) {
			Entry<Vector<Double>,String> currentK = bestKValues[i];
			if (!classNameToTotal.containsKey(currentK.getValue())) {
				classNameToTotal.put(currentK.getValue(), 1);
			} else {
				classNameToTotal.put(currentK.getValue(), classNameToTotal.get(currentK.getValue()) + 1);
			}
		}
		String bestClass = null;
		String[] equals = new String[2];
		for (Entry<String,Integer> e : classNameToTotal.entrySet()) {
			if (bestClass == null || e.getValue() > classNameToTotal.get(bestClass)) {
				bestClass = e.getKey();
			} else if (e.getValue() == classNameToTotal.get(bestClass)) {
				if (equals[0] == null) {
					equals = new String[3];
					equals[0] = e.getKey();
					equals[1] = bestClass;
				} else {
					if (equals[equals.length-1] != null) {
						String[] newArr = Arrays.copyOf(equals,equals.length+2);
						equals = newArr;
					}
					equals[equals.length-1] = e.getKey();
				}
			} else if (e.getValue() > classNameToTotal.get(bestClass)) {
				equals = new String[2];
				bestClass = e.getKey();
			}
		}
		if (equals[0] != null) {
			return equals[(int)(Math.random() * equals.length)];
		} else {
			return bestClass;
		}
	}
}
