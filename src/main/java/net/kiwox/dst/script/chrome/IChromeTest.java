package net.kiwox.dst.script.chrome;

import net.kiwox.dst.script.chrome.driver.ChromeHttpDriver;
import net.kiwox.dst.script.pojo.TestResult;

public interface IChromeTest {
	
	TestResult runTest(ChromeHttpDriver driver) throws InterruptedException;
	
}
