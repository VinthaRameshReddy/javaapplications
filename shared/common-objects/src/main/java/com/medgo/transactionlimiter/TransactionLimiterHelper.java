package com.medgo.transactionlimiter;

public class TransactionLimiterHelper {

	String mobileNo;
	String deviceId;
	int maxCount;
	String countColumnName;
	String resetTimeColumnName;
	String allowedMaxCountColumnName;

	public TransactionLimiterHelper() {
		super();
	}

	public TransactionLimiterHelper(String mobileNo, String deviceId, String countColumnName,
			String resetTimeColumnName, String allowedMaxCountColumnName) {
		super();
		this.mobileNo = mobileNo;
		this.deviceId = deviceId;
		this.countColumnName = countColumnName;
		this.resetTimeColumnName = resetTimeColumnName;
		this.allowedMaxCountColumnName = allowedMaxCountColumnName;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	public String getCountColumnName() {
		return countColumnName;
	}

	public void setCountColumnName(String countColumnName) {
		this.countColumnName = countColumnName;
	}

	public String getResetTimeColumnName() {
		return resetTimeColumnName;
	}

	public void setResetTimeColumnName(String resetTimeColumnName) {
		this.resetTimeColumnName = resetTimeColumnName;
	}

	public String getAllowedMaxCountColumnName() {
		return allowedMaxCountColumnName;
	}

	public void setAllowedMaxCountColumnName(String allowedMaxCountColumnName) {
		this.allowedMaxCountColumnName = allowedMaxCountColumnName;
	}

	@Override
	public String toString() {
		return "TransactionLimiterHelper [mobileNo=" + mobileNo + ", deviceId=" + deviceId + ", maxCount=" + maxCount
				+ ", countColumnName=" + countColumnName + ", resetTimeColumnName=" + resetTimeColumnName
				+ ", allowedMaxCountColumnName=" + allowedMaxCountColumnName + "]";
	}

}
