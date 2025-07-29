package com.revenga.rits.smartscheduler.lib.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.revenga.rits.smartscheduler.lib.util.Algorithm;
import com.revenga.rits.smartscheduler.lib.util.Algorithm.AlgorithmType;

class AlgorithmTest {

	
	void testMod11() {
	
		assertTrue(Algorithm.isValid("2103202301176816465000110020010000000010000000111", AlgorithmType.MOD11));
		assertTrue(Algorithm.isValid("0403201301179226110400110015010000000081234567816", AlgorithmType.MOD11));
		assertTrue(Algorithm.isValid("0503201201176001321000110010030009900641234567814", AlgorithmType.MOD11));
		assertTrue(Algorithm.isValid("1302201201176001321000120010030000050431234567814", AlgorithmType.MOD11));
	}
	
	@Test
	void generate() {
		
		String strFechaEmision = "07042023";
		String strTipoComprobante = "01";
		String strNumeroRuc = "1768164650001";
		String strTipoAmbiente = "1";
		String strSerie = "002001";
		String strSecuencial 	 = "000000002";
		String strCodigoNumerico = "00000001";
		String strTipoEmision = "1";
		
		String str = strFechaEmision + strTipoComprobante + strNumeroRuc + strTipoAmbiente + strSerie + strSecuencial
				+ strCodigoNumerico + strTipoEmision;
		
		System.out.println(String.format("MOD11 of \"%s\" is %s", str, Algorithm.mod11(str)));
	}
}
