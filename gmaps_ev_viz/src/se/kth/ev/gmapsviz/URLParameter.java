package se.kth.ev.gmapsviz;

public class URLParameter {
	public String name;
	public String value;

	public URLParameter(String n, String v) {
		name = n;
		value = v;
	}

	public String toString() {
		return name + "=" + value;
	}
}
