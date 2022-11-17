package net.kiwox.dst.script.pojo;

import net.kiwox.dst.script.appium.TestUtils;

public class TestResult {
	
	protected	 long time;
	private boolean error;
	private String code;
	private String message;
	
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
	public boolean isError() {
		return error;
	}
	public void setError(boolean error) {
		this.error = error;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}



	// long start = System.currentTimeMillis();
	/*result.setTime(System.currentTimeMillis() - start);
			result.setError(false);*/

	/*result.setError(true);
			result.setCode("DST-FACEBOOK-007");
			TestUtils.saveScreenshot(driver);*/

	//TestUtils.saveScreenshot(driver);
}
