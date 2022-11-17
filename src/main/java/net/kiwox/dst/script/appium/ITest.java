package net.kiwox.dst.script.appium;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import net.kiwox.dst.script.pojo.TestResult;

public interface ITest {
	
	TestResult runTest(AndroidDriver<AndroidElement> driver) throws InterruptedException;
	
}
