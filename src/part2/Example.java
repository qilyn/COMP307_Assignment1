package part2;

import java.util.Arrays;
import java.util.Vector;

public class Example {
	private Vector<Boolean> attributes;
	private boolean[] arrAttributes;
	private final boolean result;

//	public Example(Vector<Boolean> attributes, boolean result) {
//		this.attributes = (Vector<Boolean>) attributes.clone();
//		this.result = result;
//	}

	public Example(boolean[] attributes, boolean result) {
		arrAttributes = Arrays.copyOf(attributes, attributes.length);
		this.result = result;
	}

//	public Vector<Boolean> getAttributes() {
//		return (Vector<Boolean>) attributes.clone();
//	}

	public boolean[] getAttributes() {
		return arrAttributes.clone();
	}

	public boolean result() {
		return result;
	}
}
