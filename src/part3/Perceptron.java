package part3;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

/* current task: make it read test files
 * make the test files say something.
 */
public class Perceptron {
	public static final int MIN_FEATURE_MATCHES = 3;
	public static final int NUM_FEATURES = 4;
	public static final double DEFAULT_WEIGHT = 0.2;
	public static final String TRUE_CLASS = "#Yes";
	public static final double LEARNING_RATE = 0.5;
	public static final long DEFAULT_SEED = 1022119542;
	
	private List<Feature> features;
	private String trainingFile = "./src/part3/data/image.data";
	private int w;
	private int h;
	
	List<String> classifierNames;

	public static void main(String[] args) {
		Perceptron p = null;
		if (args.length == 1)
			p = new Perceptron(args[0]);
		else {
			System.err.println("The perceptron requires a training file as an argument.");
			System.exit(-1);
		}
		p.constructAndRunPerceptron();
	}

	public Perceptron() {
		readImageFiles();
	}

	public Perceptron(String training) {
		trainingFile = training;
		readImageFiles();
	}
	
	private void constructAndRunPerceptron() {
		HashMap<Image,String> images = readImageFiles();
		features = constructFeatures(50, 0);
		trainPerceptron(images);
		// recall: each input has a weight
		// each neuron has an output based on the sum product of all input + weight
		// if sum of all products of weight and input is greater than threshold, we get an output 1 else 0
		// input = features, weight = our changing values
		// sigmoid function in multilayer nn
		// replace x0 with 1, let w0 = -T (or T)
		// and change the rule: if the sum of wixi > 0 then 1, else 0
		// usually weights are initialised as small random numbers
		// if you get a higher output than desired weights are too large
		// 	subtract feature vector from weight and get new weight
		// if you present a positive value where the result is wrong, weights are too small
		// att the feeature vector to the initial weight
	}

	private void trainPerceptron(HashMap<Image,String> knownImages) {
		// uses features as inputs
		int epoch = 0;
		int correctImages = 0;
		while (epoch < 1000 && correctImages != knownImages.size()) {
			correctImages = 0;
			for (Entry<Image,String> e : knownImages.entrySet()) {
				Image img = e.getKey();
				double sumTriggeredWeights = 0;
				for (int i = 0; i < features.size(); i++) {
					Feature f = features.get(i);
					if (f.eval(img)) { // because 0*weight is 0 and 1*weight is weight.
						sumTriggeredWeights += f.getWeight(); 
					}
				}
				if (sumTriggeredWeights > 0 && !img.className.equals(TRUE_CLASS)) {
					// if we fire and it's not true, decrement all feature weights
					for (Feature f : features) {
						double newWeight = f.getWeight() + 
								LEARNING_RATE * f.getWeight() - 1;
						f.setWeight(newWeight);
					}
				} else if (sumTriggeredWeights <= 0 && img.className.equals(TRUE_CLASS)) {
					// if we don't fire and we should have, increment all feature weight
					for (Feature f : features) {
						double newWeight = f.getWeight() + 
								LEARNING_RATE * f.getWeight() + 1;
						f.setWeight(newWeight);
					}
				} else {
					correctImages ++;
				}
			}
			epoch++;
		}
		if (epoch == 1000) {
			System.out.println("Finished learning because we hit the maximum number of repetitions.");
		}
		if (correctImages == knownImages.size()) {
			System.out.println("Finished learning because we learned all "+knownImages.size()+" features in "+epoch+" steps");
		}
		printResult(knownImages);
	}
	
	private void printResult(HashMap<Image,String> knownImages) {
		int count = 0;
		int totalWrong = 0;
		for (Entry<Image,String> e : knownImages.entrySet()) {
			Image img = e.getKey();
			double sumTriggeredWeights = 0;
			for (Feature f : features) {
				if (f.eval(img)) { // because 0*weight is 0 and 1*weight is weight.
					sumTriggeredWeights += f.getWeight(); 
				}
			}
			if (sumTriggeredWeights > 0 && !img.className.equals(TRUE_CLASS)) {
				System.err.println("Image["+count+"] was triggered ("+sumTriggeredWeights+") but the class is not true.");
				totalWrong++;
			} else if (sumTriggeredWeights <= 0 && img.className.equals(TRUE_CLASS)) {
				System.err.println("Image["+count+"] was not triggered ("+sumTriggeredWeights+") but the class is true.");
				totalWrong++;
			}
			count++;
		}
		for (Feature f : features) {
			System.out.println(f.toString());
		}
		System.out.println("Total wrong = "+totalWrong +"/"+knownImages.size());
	}

	/** First method for setting up the program: read through the PCM images.  */
	public HashMap<Image,String> readImageFiles() {
		// first, read the training data into some storage location
		HashMap<Image,String> classifiedImages = new HashMap<>();
		try {
			Scanner s = new Scanner(new File(trainingFile));
			while (s.hasNextLine()) {
				readOnePcm(s, classifiedImages);
			}
			s.close();
		} catch (FileNotFoundException e) {
			System.err.println("Training file not found, you should have put tests for this in main.");
			System.err.println("Attempted to read "+trainingFile+".");
			System.exit(-1);
		}
		return classifiedImages;
	}

	/** Parses each PCM image in the file, adding it as an entry in the destination map. */
	private void readOnePcm(Scanner s, HashMap<Image,String> destination) {
		if (s.hasNextLine()) {
			String st = s.nextLine();
			if (st.charAt(0) != 'P' || st.charAt(1) != '1') return;
		}
		// read class (comment)
		Scanner scanLine = new Scanner(s.nextLine());
		String imgClass = scanLine.next();
		scanLine.close();
		// read dimensions
		scanLine = new Scanner(s.nextLine());
		w = scanLine.nextInt();
		h = scanLine.nextInt();
		boolean[][] img = new boolean[w][h];
		int x = 0;
		int y = 0;
		// read pixel values
		while (x+y*h < w*h-1 && s.hasNextLine()) {
			scanLine = new Scanner(s.nextLine());
			scanLine.useDelimiter("");
			while (scanLine.hasNextInt() && x+y*h < w*h-1) {
				img[x][y] = scanLine.nextInt()==1;
				if (x == w-1 && y < h) {
					y++;
					x = 0;
				}
				x++;
			}
			scanLine.close();
		}
		destination.put(new Image(img,imgClass),imgClass);
	}

	/** This method takes a total number of features and a seed, then returns a list of num Features.
	 * If passed a seed of 0, uses DEFAULT_SEED instead. 
	 * @param num Number of instances to fill featureX with; requires at least 50.
	 * @param seed Seed for the RNG. 
	 */
	private List<Feature> constructFeatures(int num, long seed) {
		List<Feature> list = new ArrayList<Perceptron.Feature>();
		if (num < 50) {
			System.err.println("The Perceptron must use at least 50 features to, well, meet the requirements of this assignment.");
			num = 50;
		}
		if (seed == 0) {
			System.err.println("Parameter seed = 0. Using the default seed.");
			seed = DEFAULT_SEED;
		}

		Random r = new Random(seed);

		for (int i = 0; i < num; i++) {
			int[] row = {r.nextInt(w),r.nextInt(w),r.nextInt(w),r.nextInt(w)};
			int[] col = {r.nextInt(h),r.nextInt(h),r.nextInt(h),r.nextInt(h)};
			boolean[] sgn = {r.nextBoolean(),r.nextBoolean(),r.nextBoolean(),r.nextBoolean()};
			list.add(new Feature(row,col,sgn,r.nextDouble()));
			System.out.println(list.get(i).toString());
		}
		// finally, the dummy value!
		// in hindsight, this seems like a dreadful implementation. But we'll live with it. Mostly.  
		list.add(new Feature(null,null,null,r.nextDouble()) {
			public int[] getRows() { return new int[0]; }
			public int[] getCols() { return new int[0]; }
			boolean eval(Image i) {
				return true;
			}
			public String toString() {
				return "Dummy value\t= "+1;
			}
		});
		return list;
	}

	/** Simple model of a PBM image. */
	class Image {
		public final boolean[][] data;
		public final String className;
		public final boolean classNameIsTrue;
		
		Image(boolean[][]data, String className) {
			this.data = data; // FIXME UNSAFE
			this.className = className;
			classNameIsTrue = className.equals(TRUE_CLASS);
		}

		public String toString() {
			String s = "";
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[0].length; j++) {
					s+=(data[i][j]?"X":" ");
				}
				s+="\n";
			}
			return s;
		}
	}

	/** Simple model of a feature. */
	class Feature {
		private final int[] row;
		private final int[] col;
		private final boolean [] sgn;
		
		private double weight;

		Feature(int[] row, int[] col, boolean[] sgn, double weight) {
			this.row = row;
			this.col = col;
			this.sgn = sgn;
			this.weight = weight;
		}
		
		public void setWeight(double w) {
			weight = w;
		}
		
		public double getWeight() {
			return weight;
		}
		
		public int[] getRows() {
			return Arrays.copyOf(row,row.length);
		}
		
		public int[] getCols() {
			return Arrays.copyOf(col,col.length);
		}
		
		public boolean[] getResult() {
			return Arrays.copyOf(sgn,sgn.length);
		}

		/** Evaluate a given image against this feature set. */
		boolean eval(Image img) {
			double sum = 0;
			for (int i = 0; i < row.length && i < col.length; i++) {
				if (img.data[row[i]][col[i]] == sgn[i]) 
					sum ++;
			}
			return sum >= MIN_FEATURE_MATCHES;
		}
		
		public String toString () {
			String s = "";
			for (int i = 0; i < row.length; i++) {
				s+= "("+row[i]+","+col[i]+" = "+sgn[i]+")\t";
			}
			return s + " = "+ weight;
		}
	}
}
