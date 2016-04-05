package part2;

import java.util.Arrays;
import java.util.List;

public class Example {
	private List<Boolean> attributes;
	private boolean[] arrAttributes;
	private final String result;

//	public Example(Vector<Boolean> attributes, boolean result) {
//		this.attributes = (Vector<Boolean>) attributes.clone();
//		this.result = result;
//	}

	public Example(String classifier, boolean[] attributes) {
		arrAttributes = Arrays.copyOf(attributes, attributes.length);
		this.result = classifier;
	}

//	public Vector<Boolean> getAttributes() {
//		return (Vector<Boolean>) attributes.clone();
//	}

	public boolean[] getAttributes() {
		return arrAttributes.clone();
	}

	public String result() {
		return result;
	}
}
