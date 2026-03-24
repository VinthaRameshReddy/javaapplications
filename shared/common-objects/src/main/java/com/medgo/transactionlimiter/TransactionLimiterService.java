package com.medgo.transactionlimiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionLimiterService {
	private static Logger logger = LoggerFactory.getLogger(TransactionLimiterService.class);

	public static boolean validate(TransactionLimiterHelper transactionLimiterHelper) {
		boolean limitExceedFlag = false;

		if (transactionLimiterHelper == null) {
			logger.info("transactionLimiterHelper is empty");
			return false;
		}
		logger.info("inside validate().. {}", transactionLimiterHelper.toString());

		if (transactionLimiterHelper.getDeviceId() != null && !transactionLimiterHelper.getDeviceId().isEmpty()) {
			try {
				int maxcount = TransactionLimiterDao
						.getMaxCount(transactionLimiterHelper.getAllowedMaxCountColumnName());
				transactionLimiterHelper.setMaxCount(maxcount);
			} catch (Exception e) {
				return false;
			}
			limitExceedFlag = TransactionLimiterDao.checkAndProcessTransaction(transactionLimiterHelper);
		} else {
			logger.info("DeviceId is empty");
		}
		logger.info("limitExceedFlag {}", limitExceedFlag);
		return limitExceedFlag;
	}
}
