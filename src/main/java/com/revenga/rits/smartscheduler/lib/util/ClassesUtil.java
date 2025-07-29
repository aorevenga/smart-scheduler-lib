package com.revenga.rits.smartscheduler.lib.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;


public class ClassesUtil {
	
	private static final String CLASS_EXTENSION = ".class";
	
	private ClassesUtil() {
	    throw new IllegalStateException(this.getClass().getSimpleName());
	  }

	/**
	 * Scans all classes accessible from the context class loader which belong to
	 * the given package and subpackages.
	 *
	 * @param packageName The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public static Class<?>[] getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class<?>> classes = new ArrayList<>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base
	 *                    directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(CLASS_EXTENSION)) {
				classes.add(
						Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}
	
	public static Class<?> getModelClass(String simpleClassName) throws ClassNotFoundException {
		
		return Class.forName("com.revenga.rits.back.data.core.model." + simpleClassName);
	}
	
	public static Class<?> getCommandClass(String simpleClassName) throws ClassNotFoundException {
		
		return Class.forName("com.revenga.rits.back.data.core.model.command." + simpleClassName);
	}

	/**
	 * Private helper method
	 * 
	 * @param directory The directory to start with
	 * @param pckgname  The package name to search for. Will be needed for getting
	 *                  the Class object.
	 * @param classes   if a file isn't loaded but still is in the directory
	 * @throws ClassNotFoundException
	 */
	private static void checkDirectory(File directory, String pckgname, ArrayList<Class<?>> classes)
			throws ClassNotFoundException {
		File tmpDirectory;

		if (directory.exists() && directory.isDirectory()) {
			final String[] files = directory.list();

			for (final String file : files) {
				if (file.endsWith(".class")) {
					try {
						classes.add(Class.forName(pckgname + '.' + file.substring(0, file.length() - 6)));
					} catch (final NoClassDefFoundError e) {
						// do nothing. this class hasn't been found by the
						// loader, and we don't care.
					}
				} else if ((tmpDirectory = new File(directory, file)).isDirectory()) {
					checkDirectory(tmpDirectory, pckgname + "." + file, classes);
				}
			}
		}
	}

	/**
	 * Private helper method.
	 * 
	 * @param connection the connection to the jar
	 * @param pckgname   the package name to search for
	 * @param classes    the current ArrayList of all classes. This method will
	 *                   simply add new classes.
	 * @throws ClassNotFoundException if a file isn't loaded but still is in the jar
	 *                                file
	 * @throws IOException            if it can't correctly read from the jar file.
	 */
	private static void checkJarFile(JarURLConnection connection, String pckgname, ArrayList<Class<?>> classes)
			throws ClassNotFoundException, IOException {
		final JarFile jarFile = connection.getJarFile();
		final Enumeration<JarEntry> entries = jarFile.entries();
		String name;

		for (JarEntry jarEntry = null; entries.hasMoreElements() && ((jarEntry = entries.nextElement()) != null);) {
			name = jarEntry.getName();

			if (name.contains(".class")) {
				name = name.substring(0, name.length() - 6).replace('/', '.');

				if (name.contains(pckgname)) {
					classes.add(Class.forName(name));
				}
			}
		}
	}

	/**
	 * Attempts to list all the classes in the specified package as determined by
	 * the context class loader
	 * 
	 * @param pckgname the package name to search
	 * @return a list of classes that exist within that package
	 * @throws ClassNotFoundException if something went wrong
	 */
	public static Class<?>[] getClassesForPackage(String pckgname) throws ClassNotFoundException {
		final ArrayList<Class<?>> classes = new ArrayList<>();

		try {
			final ClassLoader cld = Thread.currentThread().getContextClassLoader();

			if (cld == null)
				throw new ClassNotFoundException("Can't get class loader.");

			final Enumeration<URL> resources = cld.getResources(pckgname.replace('.', '/'));
			URLConnection connection;

			for (URL url = null; resources.hasMoreElements() && ((url = resources.nextElement()) != null);) {
				try {
					connection = url.openConnection();

					if (connection instanceof JarURLConnection) {
						checkJarFile((JarURLConnection) connection, pckgname, classes);
					} else if (connection instanceof URLConnection) {
						try {
							checkDirectory(new File(URLDecoder.decode(url.getPath(), "UTF-8")), pckgname, classes);
						} catch (final UnsupportedEncodingException ex) {
							throw new ClassNotFoundException(
									pckgname + " does not appear to be a valid package (Unsupported encoding)", ex);
						}
					} else {
						throw new ClassNotFoundException(
								pckgname + " (" + url.getPath() + ") does not appear to be a valid package");
					}
				} catch (final IOException ioex) {
					throw new ClassNotFoundException(
							"IOException was thrown when trying to get all resources for " + pckgname, ioex);
				}
			}
		} catch (final NullPointerException ex) {
			throw new ClassNotFoundException(
					pckgname + " does not appear to be a valid package (Null pointer exception)", ex);
		} catch (final IOException ioex) {
			throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname,
					ioex);
		}

		return classes.toArray(new Class[classes.size()]);
	}
	
	public static List<Field> getFields(Class<?> clazz) {
		
		List<Field> fields = new ArrayList<>();

		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

		Class<?> superClazz = clazz.getSuperclass();

		if (superClazz != null) {

			List<Field> fieldsSuper = Arrays.asList(superClazz.getDeclaredFields());
			fields.addAll(fieldsSuper);
		}
		
		return fields;
	}
	
	public static Field getField(Class<?> clazz, String name) {
		
		Field field = null;
		
		if (!name.isEmpty()) {
			
			try {
				
				field = clazz.getDeclaredField(name);
				
			} catch (NoSuchFieldException e) {
								
			}			
			
			if (field == null && clazz.getSuperclass() != null) {
				
				try {
					
					field = clazz.getSuperclass().getDeclaredField(name);
					
				} catch (NoSuchFieldException e) {
				
				} 
			}
		}
		
		return field;
	}
	
	public static Field setField(Object object, String fieldName, String value) {

		Class<?> clazz = object.getClass();
		Field field = null;
		Object objectValue = null;

		if (clazz != null) {

			try {
				
				field = getField(clazz, fieldName);
								
				if (field != null) {

					field.setAccessible(true);

					objectValue = getValueFromString(field, value);
					
					if (objectValue != null) {
						
						field.set(object, objectValue);	
					}
				}
			} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
				
				System.out.println(e.getMessage());
				System.out.println(ExceptionUtils.getStackTrace(e));
			} 
		}

		return field;
	}
	
	public static Field setField(Object object, Field field, Object value) {

		Class<?> clazz = object.getClass();

		if (clazz != null) {

			try {
				
				if (field != null) {

					field.setAccessible(true);

					field.set(object, value);	

				}
			} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
				
				System.out.println(e.getMessage());
				System.out.println(ExceptionUtils.getStackTrace(e));
			} 
		}

		return field;
	}
	
	public static Object getValueFromString(Field field, String value) {
		
		Object objValue = null;
		
		if (value == null || field == null) {
			
			return null;
		}
		
		objValue = value;
		
		if (field.getType() == Integer.class) {
			
			objValue = Integer.valueOf(value);
		}
		else if (field.getType() == Long.class) {
			
			objValue = Long.valueOf(value);
		}
		else if (field.getType() == Double.class) {
			
			objValue = Double.valueOf(value);
		}
		else if (field.getType() == Float.class) {
	
			objValue = Float.valueOf(value);
		}
		// TODO: Conversión de a tipo Date
		else if (field.getType() == Date.class) {
			
		}
		else if (field.getType() == Boolean.class) {
			
			objValue = Boolean.valueOf(value);
		}
		else {
		
			if (field.getGenericType().toString().contains(List.class.getName())) {
				
				ParameterizedType pt = (ParameterizedType) field.getGenericType() ;

				String stringList = value.replace("[", "");
				
				stringList = stringList.replace("]", "");
				stringList = stringList.replace(" ", "");
				
				String[] stringArray = stringList.split(",");
				
				if (stringArray.length > 0 && pt.getActualTypeArguments().length == 1) {
						
					Type t = pt.getActualTypeArguments()[0];
	            
					if (t == Integer.class) {
						
						objValue = Arrays.asList(stringArray).stream().map(Integer::parseInt).collect(Collectors.toList());
					}
					else if (t == Long.class) {
						
						objValue = Arrays.asList(stringArray).stream().map(Long::parseLong).collect(Collectors.toList());
					}
					else if (t == Double.class) {
						
						objValue = Arrays.asList(stringArray).stream().map(Double::parseDouble).collect(Collectors.toList());
					}
					else if (t == Float.class) {
						
						objValue = Arrays.asList(stringArray).stream().map(Float::parseFloat).collect(Collectors.toList());
					}
					else if (t == String.class) {
						
						objValue = Arrays.asList(stringArray);
					}
				}
			}
		}
		
		return objValue;
	}
}
