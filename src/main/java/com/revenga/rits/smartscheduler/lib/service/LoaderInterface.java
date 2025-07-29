package com.revenga.rits.smartscheduler.lib.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public interface LoaderInterface {
	
	public static final String LOADER_INTERFACE_LOAD_METHOD = "load";
	public static final String LOADER_INTERFACE_START_METHOD = "start";
	public static final String LOADER_INTERFACE_STOP_METHOD = "stop";
	public static final String LOADER_INTERFACE_RUNONCE_METHOD = "runonce";
	public static final String LOADER_INTERFACE_READ_CONFIG_METHOD = "readConfig";
	public static final String LOADER_INTERFACE_WRITE_CONFIG_METHOD = "writeConfig";
	public static final String LOADER_INTERFACE_READ_VERSION_METHOD = "readVersion";
	public static final String LOADER_INTERFACE_READ_LIBS_USED = "readLibsUsed";
	
	public class LoaderInterfaceException extends Exception {

		private static final long serialVersionUID = -4720721272962220496L;
		
		public LoaderInterfaceException(String message) {
			
			super(message);
		}
	}
	
	public static LoaderInterface newInstance(String className, String configFile) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException  {
		
		Class<?> clazz = Class.forName(className);
		
		if (StringUtils.isEmpty(configFile)) {

			return (LoaderInterface) clazz.getDeclaredConstructor().newInstance();
			
		} else {
			
			return (LoaderInterface) clazz.getDeclaredConstructor(String.class).newInstance(configFile);
		}
	}
	
	public static Object execute(LoaderInterface instance, String methodName) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, LoaderInterfaceException {
		
		Object obj = null;
		
		if (instance instanceof LoaderInterface) {
			
			Method method = null;
			
			try {
				
				method = instance.getClass().getDeclaredMethod(methodName);
				
			} catch (NoSuchMethodException e) {
				// Not found in child class
			}
			
			if (method == null && instance.getClass().getSuperclass() != null) {
				
				try {
					
					method = instance.getClass().getSuperclass().getDeclaredMethod(methodName);
					
				} catch (NoSuchMethodException e) {
					// Not found in parent class
				} 
			}
			
			if (method == null && instance.getClass().getSuperclass() != null && 
					instance.getClass().getSuperclass().getSuperclass() != null) {
				
				try {
					
					method = instance.getClass().getSuperclass().getSuperclass().getDeclaredMethod(methodName);
				} catch (NoSuchMethodException e) {
					
					// Not found in AbstractManager.class
				}
				
			}
			
			if (method != null) {
			
				obj = method.invoke(instance);
			}
			else {
			
				throw new LoaderInterfaceException(methodName + " not found");
			}
		}
		else {
			
			throw new LoaderInterfaceException("instance of LoaderInterface is null");
		}
		
		return obj;
	}
	
	public static Object execute(LoaderInterface instance, String methodName, Object... args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, LoaderInterfaceException {
		
		Object obj = null;
		
		if (instance instanceof LoaderInterface) {
			
			Class<?>[] types = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
			
			if (!ArrayUtils.isEmpty(types)) {
				
				Method method = null;
			
				try {
					
					method = instance.getClass().getDeclaredMethod(methodName, types);
					
				} catch (NoSuchMethodException e1) {
					
					try {
						
						if (instance.getClass().getSuperclass() != null) {
							
							method = instance.getClass().getSuperclass().getDeclaredMethod(methodName, types);
						}
						
					} catch (NoSuchMethodException e2) {
						
						try {
							
							if (instance.getClass().getSuperclass().getSuperclass() != null) {
							
								method = instance.getClass().getSuperclass().getSuperclass().getDeclaredMethod(methodName, types);
							}
							
						} catch (NoSuchMethodException e3) {
							// No found in child class
						}
					}
				}
				
				if (method != null) {
					
					obj = method.invoke(instance, args);
				}
				else {
				
					throw new LoaderInterfaceException(methodName + " not found");
				}
			}
		}
		else {
			
			throw new LoaderInterfaceException("instance of LoaderInterface is null");
		}
		
		return obj;
	}
}
