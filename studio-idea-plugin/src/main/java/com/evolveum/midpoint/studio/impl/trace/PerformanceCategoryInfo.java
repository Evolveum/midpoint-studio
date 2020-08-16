package com.evolveum.midpoint.studio.impl.trace;

public class PerformanceCategoryInfo {
	private long ownTime;
	private long totalTime;
	private int ownCount;
	private int totalCount;
	public long getOwnTime() {
		return ownTime;
	}
	public void setOwnTime(long ownTime) {
		this.ownTime = ownTime;
	}
	public long getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}
	public int getOwnCount() {
		return ownCount;
	}
	public void setOwnCount(int ownCount) {
		this.ownCount = ownCount;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	
}
