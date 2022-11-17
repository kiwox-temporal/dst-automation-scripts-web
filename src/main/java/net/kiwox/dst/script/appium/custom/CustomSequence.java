package net.kiwox.dst.script.appium.custom;

import org.openqa.selenium.interactions.InputSource;
import org.openqa.selenium.interactions.Interaction;
import org.openqa.selenium.interactions.Sequence;

public class CustomSequence extends Sequence {
	
	private int size;

	public CustomSequence(InputSource device, int initialLength) {
		super(device, initialLength);
		this.size = 0;
	}

	@Override
	public Sequence addAction(Interaction action) {
		Sequence seq = super.addAction(action);
		++size;
		return seq;
	}

	public int getSize() {
		return size;
	}

}
