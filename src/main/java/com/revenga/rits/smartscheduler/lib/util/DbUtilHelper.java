package com.revenga.rits.smartscheduler.lib.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

public class DbUtilHelper {

	private static final int TRACE_RECORDS = 1000;
	private static final int DEFAULT_UPDATE_SIZE = 1000;

	private DbUtilHelper() {
		throw new IllegalStateException(this.getClass().getSimpleName());
	}

	public static void execSentenceInsertSelect(String insertSelectQuery, Connection connDB, Integer bathSize,
			Boolean commit, Logger log) throws Exception {

		long rows = 0;
		Instant startInstant = Instant.now();

		System.out.println("Start insert select,  bathSize: " + bathSize);

		if (!StringUtils.isEmpty(insertSelectQuery)) {

			String[] result = extractSelectAndTable(insertSelectQuery);

			rows = insertFromSelect(result[2], result[0], result[1], connDB, bathSize, commit, log);
		}
		System.out.println("End insert select, records inserted " + rows + " in: "
				+ Duration.between(startInstant, Instant.now()).toMillis() + " ms");

	}

	public static void execSentenceUpdate(String updateQuery, String fieldId, Connection connDB, Integer updateSize,
			Integer bathSize, Integer commitSize, Logger log) throws Exception {

		long rows = 0;
		Instant startInstant = Instant.now();

		log.info(
				"Start update, bathSize: " + bathSize + ", commitSize: " + commitSize + ", updateSize: " + updateSize);

		if (!StringUtils.isEmpty(updateQuery)) {

			String[] result = extractSetAndWhere(updateQuery);

			rows = updateFromSelect(result[0], result[1], result[2], fieldId, connDB, updateSize, bathSize, commitSize,
					log);
		}
		log.info("End update, records updated " + rows + " in: "
				+ Duration.between(startInstant, Instant.now()).toMillis() + " ms");

	}

	private static long updateFromSelect(String table, String setClause, String whereClause, String fieldId,
			Connection connDB, Integer updateSize, Integer bathSize, Integer commitSize, Logger log) throws Exception {

		long rows = 0;

		if (!StringUtils.isEmpty(table) && !StringUtils.isEmpty(setClause) && !StringUtils.isEmpty(whereClause)
				&& connDB != null) {

			
			String field = (fieldId != null ? fieldId : "ID");

			String selectQuery = "SELECT " + field + " " + "FROM " + table + " " + whereClause;
			String updateQuery = "UPDATE " + table + " " + setClause + " WHERE " + field + " in (";

			log.info(String.format("Select --> Select query: %s", selectQuery));
			log.info(String.format("Update --> Update query: %s", updateQuery));

			//selectQuery = SqlParser.applySecurity(selectQuery);
			
			try (Statement stmtUpdate = connDB.createStatement();
					Statement stmtDB = connDB.createStatement();
					ResultSet resultSet = stmtDB.executeQuery(selectQuery)) {

				List<String> listIds = new ArrayList<>();

				int countTocommint = 0;
				int countTobatch = 0;

				int maxList = updateSize != null ? updateSize : DEFAULT_UPDATE_SIZE;
				int iBathSize = bathSize != null ? bathSize : 0;
				int iCommitSize = commitSize != null ? commitSize : 0;

				String sql = null;
				
				while (resultSet.next()) {

					listIds.add(String.valueOf(resultSet.getLong(field)));

					if (listIds.size() >= maxList) {

						sql  = updateQuery + String.join(",", listIds) + ")";
						//sql = SqlParser.applySecurity(sql);
					
						if (iBathSize > 0) {

							stmtUpdate.addBatch(sql);
							countTobatch++;

						} else {

							stmtUpdate.executeUpdate(sql);
							countTocommint++;
						}
						listIds.clear();
					}

					if (iBathSize > 0 && countTobatch >= iBathSize) {

						stmtUpdate.executeBatch();
						countTobatch = 0;
						countTocommint++;
					}

					if (iCommitSize > 0 && countTocommint >= iCommitSize) {
						
						connDB.commit();
						countTocommint = 0;
					}

					rows++;
				}

				if (!listIds.isEmpty()) {

					sql = updateQuery + String.join(",", listIds) + ")";
					//sql = SqlParser.applySecurity(sql);

					if (iBathSize > 0) {

						stmtUpdate.addBatch(sql);
						countTobatch++;

					} else {
						stmtUpdate.executeUpdate(sql);
						countTocommint++;
					}
				}

				if (iBathSize > 0 && countTobatch > 0) {

					stmtUpdate.executeBatch();
					countTocommint++;
				}

				if (iCommitSize > 0 && countTocommint > 0) {

					connDB.commit();
				}
			}

		} else if (log != null) {

			log.error(String.format("updateFromSelect -->  empty value, table: %s setClause: %s whereClause: %s", table,
					setClause, whereClause));
		}

		return rows;
	}

	public static long insertFromSelect(String selectQuery, String targetTable, String targetColumn, Connection connDB,
			Integer bathSize, Boolean commit, Logger log) throws Exception {

		long rows = 0;

		if (!StringUtils.isEmpty(selectQuery) && !StringUtils.isEmpty(targetTable) && connDB != null) {

			/*
			 * log.info(String.
			 * format("DbUtilHelper:insertFromSelect selectQuery: %s \n targetTable: %s targetColumn: %s"
			 * , selectQuery, targetTable, targetColumn));
			 * 
			 * String sql = SqlParser.applySecurity(selectQuery);
			 */

			System.out.println(String.format("InsertFromSelect --> Select query: %s", selectQuery));

			try (Statement stmtDB = connDB.createStatement(); ResultSet resultSet = stmtDB.executeQuery(selectQuery)) {

				ResultSetMetaData metaData = resultSet.getMetaData();
				int numColumnas = metaData.getColumnCount();

				if (numColumnas > 0) {

					String sql = getSentenceInsert(targetTable, targetColumn, numColumnas);

					if (!StringUtils.isEmpty(sql)) {

						/*
						 * sql = SqlParser.applySecurity(sql);
						 * 
						 * log.info(String.format("DbUtilHelper:insertFromSelect insertQuery: %s",
						 * sql));
						 */

						System.out.println(String.format("InsertFromSelect --> Insert query: %s", sql));

						try (PreparedStatement pstmtInsert = connDB.prepareStatement(sql)) {

							rows = execSentenceInsert(resultSet, numColumnas, pstmtInsert, bathSize, log);
						}
					}

					if (commit != null && commit.booleanValue()) {

						connDB.commit();
					}
				}

			}

		} else if (log != null) {

			log.error(String.format("InsertFromSelect -->  empty value, selectQuery: %s targetTable: %s", selectQuery,
					targetTable));
		}

		return rows;
	}

	private static String getSentenceInsert(String targetTable, String targetColumn, int numColumnas) {

		String result = null;

		if (!StringUtils.isEmpty(targetTable)) {

			StringBuilder insertQuery = new StringBuilder(
					"INSERT INTO " + targetTable + " " + targetColumn + " " + "VALUES (");

			for (int i = 0; i < numColumnas; i++) {

				insertQuery.append("?");

				if (i < numColumnas - 1) {

					insertQuery.append(", ");
				}
			}
			insertQuery.append(")");

			result = insertQuery.toString();
		}

		return result;
	}

	private static String[] extractSelectAndTable(String sqlQuery) {

		String[] result = new String[3];

		Pattern pattern = Pattern.compile("INSERT\\s+INTO\\s+([a-zA-Z0-9_]+)\\s*(\\(.*?\\))\\s*(SELECT\\s+.*)",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sqlQuery);

		if (matcher.find()) {

			result[0] = matcher.group(1).trim();
			result[1] = matcher.group(2).trim();
			result[2] = matcher.group(3).trim();
		}

		return result;
	}

	private static String[] extractSetAndWhere(String sqlQuery) {

		String[] result = new String[3];

		Pattern pattern = Pattern.compile("UPDATE\\s+([a-zA-Z0-9_]+)\\s+(SET\\s+.*?)\\s+(WHERE\\s+.*)",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sqlQuery);

		if (matcher.find()) {

			result[0] = matcher.group(1).trim();
			result[1] = matcher.group(2).trim();
			result[2] = matcher.group(3).trim();
		}

		return result;
	}

	private static long execSentenceInsert(ResultSet resultSet, int numColumnas, PreparedStatement pstmt,
			Integer bathSize, Logger log) throws SQLException {

		int records = 0;
		int batchRecords = 0;

		if (resultSet != null && pstmt != null) {

			while (resultSet.next()) {

				for (int i = 1; i <= numColumnas; i++) {

					Object valorColumna = resultSet.getObject(i);
					pstmt.setObject(i, valorColumna);
				}

				if (bathSize != null && bathSize.intValue() != 0) {

					pstmt.addBatch();
					batchRecords++;

					if (batchRecords == bathSize.intValue()) {

						pstmt.executeBatch();
						batchRecords = 0;
					}

				} else {

					pstmt.executeUpdate();
				}

				records++;

				if (records % TRACE_RECORDS == 0) {

					// log.info(String.format("DbUtilHelper:execSentenceInsert Batch insert: %s
					// records", records));
					System.out.println(String.format("InsertFromSelect --> Insert %s rows", records));
				}
			}

			if (batchRecords > 0) {

				pstmt.executeBatch();
			}

		}
		return records;

	}

}
