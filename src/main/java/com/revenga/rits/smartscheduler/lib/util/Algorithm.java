package com.revenga.rits.smartscheduler.lib.util;

public class Algorithm {

	public enum AlgorithmType {
		MOD11
	}
	private Algorithm() {
	
		throw new IllegalStateException(this.getClass().getSimpleName());
	}
	
	public static int mod11(String str) {
		
		int sum = 0;
        int factor = 2;
        int mod11Result = 0;
        
        for (int i = str.length() - 1; i >= 0; i--) {
        	
            int valor = Character.getNumericValue(str.charAt(i));
            sum += valor * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        
        int resto = sum % 11;
        
        mod11Result = 11 - resto;
        
        if (mod11Result >= 10) {
        	
        	mod11Result = 11 - mod11Result;
        }
        
        return mod11Result;
	}
	
	public static boolean isValid(String str, AlgorithmType algorithmType) {
		
		boolean valid = false;
		
		if (algorithmType.equals(AlgorithmType.MOD11)) {
			
			valid = isValidMod11(str);
		}
		
		return valid;
	}
	
	private static boolean isValidMod11(String str) {
		
		int res = mod11(str.substring(0, str.length() - 1));
		
		return (res == Character.getNumericValue(str.charAt(str.length() - 1)));
	}
}
