package net.kiwox.dst.script.pojo;

public class SpeedTestResult {
	
	private float upload;
	private float download;
	private long ping;
	
	public float getUpload() {
		return upload;
	}
	public void setUpload(float upload) {
		this.upload = upload;
	}
	
	public float getDownload() {
		return download;
	}
	public void setDownload(float download) {
		this.download = download;
	}
	
	public long getPing() {
		return ping;
	}
	public void setPing(long ping) {
		this.ping = ping;
	}

}
