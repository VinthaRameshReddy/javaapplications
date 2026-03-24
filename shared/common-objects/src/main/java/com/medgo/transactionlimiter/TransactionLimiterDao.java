package com.medgo.transactionlimiter;


import com.medgo.database.DBUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class TransactionLimiterDao {
	static Logger logger = LoggerFactory.getLogger(TransactionLimiterDao.class.getName());

	public static boolean checkAndProcessTransaction(TransactionLimiterHelper limiterHelper) {

		logger.info("inside checkAndProcessTransaction().. " + limiterHelper.toString());
		Connection connection = null;
		PreparedStatement selectStmt = null;
		ResultSet rs = null;
		PreparedStatement updateStmt = null;
		PreparedStatement insertStmt = null;
		boolean limitExceedFlag = false;

		try {
			connection = DBUtilities.getJndiConnection();
			String selectQuery = "SELECT " + limiterHelper.getCountColumnName() + ", "
					+ limiterHelper.getResetTimeColumnName()
					+ ", DEVICE_ID FROM USERS_OTP_LIMIT WHERE MOBILE_NUMBER = ?";
			selectStmt = connection.prepareStatement(selectQuery);
			selectStmt.setString(1, limiterHelper.getMobileNo());
			rs = selectStmt.executeQuery();

			int transactionCount = 0;
			Timestamp lastReset = null;
			String dbDeviceId = "";
			boolean exists = false;

			if (rs.next()) {
				transactionCount = rs.getInt(limiterHelper.getCountColumnName());
				lastReset = rs.getTimestamp(limiterHelper.getResetTimeColumnName());
				dbDeviceId = rs.getString("DEVICE_ID") == null ? "" : rs.getString("DEVICE_ID");
				exists = true;
			}
			logger.info(" Is device id exist>>>" + exists + "  Count >>>" + transactionCount + "  dbDeviceId::"
					+ dbDeviceId);
			logger.info(" Last resset time >>>" + lastReset);
			Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
			// for same mobile deviceId change
			if (!dbDeviceId.isEmpty()
					&& (!dbDeviceId.equalsIgnoreCase(limiterHelper.getDeviceId()))) {
				logger.info("Inside deviceId different");
				transactionCount = 0;
				lastReset = currentTimestamp;
			}
			// SAOENH-428 Allow user after 2hrs
			if("LEAD_RESUBMIT_COUNT".equalsIgnoreCase(limiterHelper.getCountColumnName())) {
				if (lastReset == null || ((currentTimestamp.getTime() - lastReset.getTime()) / 1000) > 7200) {
					logger.info("Inside lastReset condition");
					transactionCount = 0; // Reset transaction count if more than a minute has passed
					lastReset = currentTimestamp;
				}
					
			} else {
			// added 3 min need to change 1 min after testing
			if (lastReset == null || currentTimestamp.getTime() - lastReset.getTime() > 60000) {
				logger.info("Inside lastReset condition");
				transactionCount = 0; // Reset transaction count if more than a minute has passed
				lastReset = currentTimestamp;
				}
			}
			if (transactionCount >= limiterHelper.getMaxCount()) {
				// Transaction limit exceeded
				logger.info("Transaction limit exceeded for device ID: " + limiterHelper.getDeviceId());
				limitExceedFlag = true;
			} else {
				transactionCount++; // Increment the transaction count
				if (exists) {
					String updateQuery = "UPDATE USERS_OTP_LIMIT SET " + limiterHelper.getCountColumnName() + " = ?, "
							+ limiterHelper.getResetTimeColumnName() + " = ?, DEVICE_ID = ?  WHERE MOBILE_NUMBER = ?";
					updateStmt = connection.prepareStatement(updateQuery);
					updateStmt.setInt(1, transactionCount);
					updateStmt.setTimestamp(2, lastReset);
					updateStmt.setString(3, limiterHelper.getDeviceId());
					updateStmt.setString(4, limiterHelper.getMobileNo());
					int updateCount = updateStmt.executeUpdate();
					logger.info("updateCount ::" + updateCount);
				} else {
					String insertQuery = "INSERT INTO USERS_OTP_LIMIT (MOBILE_NUMBER, "
							+ limiterHelper.getCountColumnName() + " , " + limiterHelper.getResetTimeColumnName()
							+ ", DEVICE_ID) VALUES (?, ?, ?, ?)";
					insertStmt = connection.prepareStatement(insertQuery);
					insertStmt.setString(1, limiterHelper.getMobileNo());
					insertStmt.setInt(2, transactionCount);
					insertStmt.setTimestamp(3, lastReset);
					insertStmt.setString(4, limiterHelper.getDeviceId());
					int insertCount = insertStmt.executeUpdate();
					logger.info("insertCount ::" + insertCount);
				}
				logger.error("Transaction allowed for device ID: " + limiterHelper.getDeviceId());
			}
		} catch (SQLException e) {
			logger.error("Exception while transacrionLimit Process>>>" + e);
			;
		} finally {
			DBUtilities.closeResultsetObj(rs);
			DBUtilities.closePreparedStatementObj(selectStmt);
			DBUtilities.closePreparedStatementObj(updateStmt);
			DBUtilities.closePreparedStatementObj(insertStmt);
			DBUtilities.closeConnecObj(connection);
		}
		return limitExceedFlag;
	}

	public static int getMaxCount(String maxCountColumnName) throws Exception {
		PreparedStatement stmt = null;
		Connection connection = null;
		ResultSet resultSetObj = null;
		int totalCount = 0;
		String selectQuery = "";
		try {
			connection = DBUtilities.getJndiConnection();
			selectQuery = "SELECT " + maxCountColumnName + " FROM OTP_ALLOWED_LIMIT";
			stmt = connection.prepareStatement(selectQuery);
			resultSetObj = stmt.executeQuery();
			while (resultSetObj.next()) {
				totalCount = resultSetObj.getInt(maxCountColumnName);
			}
			logger.info("Configured count>> " + totalCount);
		} catch (Exception e) {
			logger.error("Exception in maxcount::" + e);
		} finally {
			DBUtilities.closeResultsetObj(resultSetObj);
			DBUtilities.closePreparedStatementObj(stmt);
			DBUtilities.closeConnecObj(connection);
		}
		return totalCount;
	}
}