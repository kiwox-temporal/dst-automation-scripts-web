package net.kiwox.dst.script.chrome.custom;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class MultipleExpectedCondition implements ExpectedCondition<Entry<Integer, WebElement>> {

	private List<By> search;
	
	public MultipleExpectedCondition(By first, By... others) {
		search = new LinkedList<>();
		search.add(first);
		Collections.addAll(search, others);
	}

	@Override
	public Entry<Integer, WebElement> apply(WebDriver driver) {
		for (int i = 0; i < search.size(); ++i) {
			List<WebElement> elements = driver.findElements(search.get(i));
			if (!elements.isEmpty()) {
				return new SimpleEntry<>(i, elements.get(0));
			}
		}
		return null;
	}

}
