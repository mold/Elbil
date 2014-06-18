package com.kth.ev.differentiatedrange.gamification;

import java.util.EmptyStackException;

public class DataAnalyzer {
	
	DataStack dataStack;
	int size;
	double range;
	double average;
	double prevAverage;
	
	class DataStack {
		
		int length = 0;
		Data top = null;
		Data bottom = null;
		
		class Data {
			public double value;
			public Data next = null;
			public Data parent = null;
			
			public Data(double value) {
				this.value = value;
			}
		}
		
		public void push(double value) {
			Data data = new Data(value);
			
			if (top == null) {
				top = data;
				bottom = top;
			} else {
				top.parent = data;
				data.next = top;
				top = data;
			}
			length++;
		}
		
		public double pop() throws EmptyStackException {
			double value;
			
			if (top != null) {
				value = top.value;
				top = top.next;
			} else {
				throw new EmptyStackException();
			}
			length--;
			
			return value;
		}
		
		public double shift() throws EmptyStackException {
			double value;
			
			if (bottom != null) {
				value = bottom.value;
				bottom = bottom.parent;
				bottom.next = null;
			} else {
				throw new EmptyStackException();
			}
			length--;
			
			return value;
		}
		
		public int length() {
			return length;
		}
		
		public double getAverage() {
			double average = 0.0;
			Data data = top;
			while (data != null) {
				average += data.value;
				data = data.next;
			}
			average = average / length;
			return average;
		}
		
		public String toString() {
			String string = "";
			Data data = top;
			while(data != null) {
				string += data.value + " ";
				data = data.next;
			}
			return string;
		}
	}
	
	public DataAnalyzer(int size, double range) {
		this.size = size;
		this.range = range;
		dataStack = new DataStack();
	}
	
	public void pushData(double data) {
		dataStack.push(data);
		if (dataStack.length() > size) {
			try {
				dataStack.shift();
			} catch(EmptyStackException ese) {
				// this should never happen
			}
		}
		prevAverage = average;
		average = dataStack.getAverage();
	}
	
	public double getAverage() {
		return average;
	}
	
	public int getStateOfChange() {
		double change = average-prevAverage;
		if (Math.abs(change) <= range) {
			return 0;
		}
		return (change < 0)? -1 : 1;
	}
	
	@Override
	public String toString() {
		return dataStack.toString();
	}
}
