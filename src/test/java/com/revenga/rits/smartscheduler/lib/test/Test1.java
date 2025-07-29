package com.revenga.rits.smartscheduler.lib.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;

import com.revenga.rits.smartscheduler.lib.util.AES;
import com.revenga.rits.smartscheduler.lib.util.DbUtilHelper;

import lombok.extern.log4j.Log4j2;

@Log4j2
class Test1 {

	//zeroDateTimeBehavior=convertToNull&
	
	String urlBatchedStatements = "jdbc:mysql://revengadb-test.cbwpyele25tv.us-east-1.rds.amazonaws.com:3306/sir_dat?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true";
	String url = "jdbc:mysql://revengadb-test.cbwpyele25tv.us-east-1.rds.amazonaws.com:3306/sir_dat?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&zeroDateTimeBehavior=convertToNull";

	String user = "admin";
	String password = "Z1GoweQA/iGwlKH1bTpkBw==";
	String sqlDelete = "DELETE FROM sir_dat.srimvmn";
	
	Integer bathSize = 5000;

	String sqlQuery_1 = "Insert Into SRIMVMN (CONCESSION_CODE, CHS_DATA_DAT, CASHMOV_TYPE, CASHMOV_DES, CASHMOV_OPE, CASHMOV_CANT, CASHMOV_IMP, FOR_AMT,"
			+ " PAYMOD_CODE, PAYMOD_DES, CE_SERIE, CE_SECUENCIAL, CE_CA,"
			+ " ID_ACCOUNT_ABT, ACCOUNT_RAZON, ACCOUNT_FISCAL_TYPE, ACCOUNT_FISCAL_ID, ACCOUNT_DIR, ACCOUNT_EMAIL,"
			+ " TKSTOP_ESTAB, TKSTOP_DIRESTAB, TKSTOP_PTOEMI,"
			+ " ESTADO, ESTADO_DAT, FECHA_AUTORIZACION, ERROR_ID, ERROR_MENSAJE,"
			+ " RUTA_XML_ENVIADO, RUTA_XML_RECIBIDO, RUTA_XML_COMPROBANTE_RECIBIDO, RUTA_PDF_GENERADO,"
			+ " LOADSRIMVMN_DATE, IDTRANSACTION, NUMDOCMODIFICADO, DATEDOCMODIFICADO, SOURCE_TABLE)" + " Select"
			+ " CASHMVMN.CONCESSION_CODE, CASHMVMN.CHS_DATE_DAT,"
			+ " Case CASHMVMN.CASHMOV_TYP When '6' Then '01' When '8' Then '04' End As CASHMOV_TYPE,"
			+ " CONCEPT.CONCEPT_DESC,"
			+ " Case CASHMVMN.CASHMOV_TYP When '6' Then '01' When '8' Then '03' End As CASHMOV_OPE,"
			+ " 1, CASHMVMN.FOR_AMT, CASHMVMN.FOR_AMT,"
			+ " PAYMOD_PAYMOD_CODE_CONVERSION.PAYMOD_CODE_SRI, PAYMOD.PAYMOD_DES, CASHMVMN.CESERIE, CASHMVMN.CESECUENCIAL, CASHMVMN.CECA,"
			+ " NULL, 'CONSUMIDOR FINAL', '07', '9999999999999', NULL, NULL,"
			+ " CASHMVMN.TKSTOP_EST, CASHMVMN.TKSTOP_ESTDIR, CASHMVMN.TKEQUIP_NUM," + " NULL, NULL, NULL, NULL, NULL,"
			+ " NULL, NULL, NULL, NULL," + " " + convertToSqlDateTime(new Date())
			+ ", CASHMVMN.IDTRANSACTION, CASHMVMN.NUMDOCMODIFICADO, CASHMVMN.DATEDOCMODIFICADO, 'CASHMVMN' SOURCE_TABLE"
			+ " From CASHMVMN"
			+ " Left Join CONCEPT On (CONCEPT.CONCEPT_CODE = 'CASHMOV_TYP' And CASHMVMN.CASHMOV_TYP = CONCEPT.CONCEPT_VALUE)"
			+ " Left Join PAYMOD On (CASHMVMN.PAYMOD_CODE = PAYMOD.PAYMOD_CODE)"
			+ " Left Join PAYMOD_PAYMOD_CODE_CONVERSION On (CASHMVMN.PAYMOD_CODE = PAYMOD_PAYMOD_CODE_CONVERSION.PAYMOD_CODE)"
			+ " Where CASHMVMN.CASHMOV_TYP In ('6','8') and CASHMVMN.CECA IS NOT NULL";

	String sqlQuery_2 = "Insert Into SRIMVMN (CONCESSION_CODE, CHS_DATA_DAT, CASHMOV_TYPE, CASHMOV_DES, CASHMOV_OPE, CASHMOV_CANT, CASHMOV_IMP, FOR_AMT," + 
			" PAYMOD_CODE, PAYMOD_DES, CE_SERIE, CE_SECUENCIAL, CE_CA," + 
			" ID_ACCOUNT_ABT, ACCOUNT_RAZON, ACCOUNT_FISCAL_TYPE, ACCOUNT_FISCAL_ID, ACCOUNT_DIR, ACCOUNT_EMAIL," +
			" TKSTOP_ESTAB, TKSTOP_DIRESTAB, TKSTOP_PTOEMI," + 
			" ESTADO, ESTADO_DAT, FECHA_AUTORIZACION, ERROR_ID, ERROR_MENSAJE," + 
			" RUTA_XML_ENVIADO, RUTA_XML_RECIBIDO, RUTA_XML_COMPROBANTE_RECIBIDO, RUTA_PDF_GENERADO," + 
			" LOADSRIMVMN_DATE, IDTRANSACTION, NUMDOCMODIFICADO, DATEDOCMODIFICADO, SOURCE_TABLE)" +
			" Select" + 
			" CASHMVMN.CONCESSION_CODE, CASHMVMN.CHS_DATE_DAT," +
			" Case CASHMVMN.CASHMOV_TYP When '7' Then '01' When '14' Then '04' End As CASHMOV_TYPE," +	
			" CONCEPT.CONCEPT_DESC," +
			" Case CASHMVMN.CASHMOV_TYP When '7' Then '02' When '14' Then '03' End As CASHMOV_OPE," +
			" 1, CASHMVMN.FOR_AMT, CASHMVMN.FOR_AMT," + 
			" PAYMOD_PAYMOD_CODE_CONVERSION.PAYMOD_CODE_SRI, PAYMOD.PAYMOD_DES, CASHMVMN.CESERIE, CASHMVMN.CESECUENCIAL, CASHMVMN.CECA," +
			" ACCOUNT_ABT_RECHARGE.ACCOUNTABT_ID, ACCOUNT_ABT.FULL_NAME, ACCOUNT_ABT_DOCUMENT_TYPE_CONVERSION.ACCOUNT_FISCAL_TYPE, ACCOUNT_ABT.DOCUMENT_ID, ACCOUNT_ABT.ADDRESS, ACCOUNT_ABT.EMAIL," + 
			" CASHMVMN.TKSTOP_EST, CASHMVMN.TKSTOP_ESTDIR, CASHMVMN.TKEQUIP_NUM," + 
			" NULL, NULL, NULL, NULL, NULL," + 
			" NULL, NULL, NULL, NULL," + 
			" " + convertToSqlDateTime(new Date()) + ", CASHMVMN.IDTRANSACTION, CASHMVMN.NUMDOCMODIFICADO, CASHMVMN.DATEDOCMODIFICADO, 'CASHMVMN' SOURCE_TABLE" +
			" From CASHMVMN" + 
			" Left Join CONCEPT On (CONCEPT.CONCEPT_CODE = 'CASHMOV_TYP' And CASHMVMN.CASHMOV_TYP = CONCEPT.CONCEPT_VALUE)" + 
			" Left Join PAYMOD On (CASHMVMN.PAYMOD_CODE = PAYMOD.PAYMOD_CODE)" +
			" Left Join PAYMOD_PAYMOD_CODE_CONVERSION On (CASHMVMN.PAYMOD_CODE = PAYMOD_PAYMOD_CODE_CONVERSION.PAYMOD_CODE)" +
			" Left Join ACCOUNT_ABT_RECHARGE On (CASHMVMN.IDTRANSACTION = ACCOUNT_ABT_RECHARGE.ID)" +
			" Left Join ACCOUNT_ABT On (ACCOUNT_ABT_RECHARGE.ACCOUNTABT_ID = ACCOUNT_ABT.ACCOUNTABT_ID)" +
			" Left Join ACCOUNT_ABT_DOCUMENT_TYPE_CONVERSION On (ACCOUNT_ABT.DOCUMENT_TYPE = ACCOUNT_ABT_DOCUMENT_TYPE_CONVERSION.DOCUMENT_TYPE)" +
			" Where CASHMVMN.CASHMOV_TYP In ('7','14') and CASHMVMN.CECA IS NOT NULL";
	
	public static String convertToSqlDateTime(Date Date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return " str_to_date('" + formatter.format(Date) + "','%Y-%m-%d %H:%i:%S')";
	}


	@Test
	void test1_1() {

		try (Connection connDB = DriverManager.getConnection(urlBatchedStatements, user, AES.decrypt(password));
				Statement stmtDB = connDB.createStatement();) {

			stmtDB.executeUpdate(sqlDelete);
			connDB.setAutoCommit(false);

			try {

				DbUtilHelper.execSentenceInsertSelect(sqlQuery_1, connDB, bathSize, true, log);
	
			} catch (Exception e) {

				System.out.println(e.getMessage());
				System.out.println(ExceptionUtils.getStackTrace(e));

				connDB.rollback();
			}

		} catch (Exception e) {

			System.out.println(e.getMessage());
			System.out.println(ExceptionUtils.getStackTrace(e));
		}
	}

	void test1_2() {


		try (Connection connDB = DriverManager.getConnection(url, user, AES.decrypt(password));
				Statement stmtDB = connDB.createStatement();) {

			stmtDB.executeUpdate(sqlDelete);
			connDB.setAutoCommit(false);

			try {

				DbUtilHelper.execSentenceInsertSelect(sqlQuery_1, connDB, 0, false, log);

			} catch (Exception e) {

				System.out.println(e.getMessage());
				System.out.println(ExceptionUtils.getStackTrace(e));

				connDB.rollback();
			}

		} catch (Exception e) {

			System.out.println(e.getMessage());
			System.out.println(ExceptionUtils.getStackTrace(e));
		}
		
	}
	

	void test1_3() {

		try (Connection connDB = DriverManager.getConnection(url, user, AES.decrypt(password));
				Statement stmtDB = connDB.createStatement();) {

			stmtDB.executeUpdate(sqlDelete);
			connDB.setAutoCommit(false);

			try {

				Instant startInstant = Instant.now();

				System.out.println("Start sql insert select");

				stmtDB.executeUpdate(sqlQuery_1);
				connDB.commit();

				System.out.println("End sql insert select in: " + Duration.between(startInstant, Instant.now()).toMillis() + " ms");

			} catch (Exception e) {

				System.out.println(e.getMessage());
				System.out.println(ExceptionUtils.getStackTrace(e));

				connDB.rollback();
			}

		} catch (Exception e) {

			System.out.println(e.getMessage());
			System.out.println(ExceptionUtils.getStackTrace(e));
		}

	}
	
	@Test
	void test1_4() {

		try (Connection connDB = DriverManager.getConnection(urlBatchedStatements, user, AES.decrypt(password));
				Statement stmtDB = connDB.createStatement();) {

			//stmtDB.executeUpdate(sqlDelete);
			connDB.setAutoCommit(false);

			try {

				DbUtilHelper.execSentenceInsertSelect(sqlQuery_2, connDB, bathSize, true, log);
	
			} catch (Exception e) {

				System.out.println(e.getMessage());
				System.out.println(ExceptionUtils.getStackTrace(e));

				connDB.rollback();
			}

		} catch (Exception e) {

			System.out.println(e.getMessage());
			System.out.println(ExceptionUtils.getStackTrace(e));
		}
		
	}
	

	void test1_5() {

		try (Connection connDB = DriverManager.getConnection(url, user, AES.decrypt(password));
				Statement stmtDB = connDB.createStatement();) {

			stmtDB.executeUpdate(sqlDelete);
			connDB.setAutoCommit(false);

			try {

				DbUtilHelper.execSentenceInsertSelect(sqlQuery_2, connDB, 0, false, log);

			} catch (Exception e) {

				System.out.println(e.getMessage());
				System.out.println(ExceptionUtils.getStackTrace(e));

				connDB.rollback();
			}

		} catch (Exception e) {

			System.out.println(e.getMessage());
			System.out.println(ExceptionUtils.getStackTrace(e));
		}
	}

	void test1_6() {
	
		try (Connection connDB = DriverManager.getConnection(url, user, AES.decrypt(password));
				Statement stmtDB = connDB.createStatement();) {

			stmtDB.executeUpdate(sqlDelete);
			connDB.setAutoCommit(false);

			try {

				Instant startInstant = Instant.now();

				System.out.println("Start sql insert select");

				stmtDB.executeUpdate(sqlQuery_2);
				connDB.commit();

				System.out.println("End sql insert select in: " + Duration.between(startInstant, Instant.now()).toMillis() + " ms");

			} catch (Exception e) {

				System.out.println(e.getMessage());
				System.out.println(ExceptionUtils.getStackTrace(e));

				connDB.rollback();
			}

		} catch (Exception e) {

			System.out.println(e.getMessage());
			System.out.println(ExceptionUtils.getStackTrace(e));
		}

	}

}
