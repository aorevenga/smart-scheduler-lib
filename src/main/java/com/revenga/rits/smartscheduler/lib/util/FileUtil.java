package com.revenga.rits.smartscheduler.lib.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;

import lombok.NonNull;

public class FileUtil {

	private FileUtil() {
		throw new IllegalStateException(this.getClass().getSimpleName());
	}

	@NonNull
	public static URL getUrl(String path) throws MalformedURLException {

		String[] parts = path.split("/");

		for (int i = 0; i < parts.length; i++) {

			String part = parts[i];

			if (part.startsWith("$")) {

				String var = part.substring(1);
				String value = System.getenv(var);

				if (value != null) {

					parts[i] = value;
				}
			}
		}

		String urlStr = "file:///" + String.join("/", parts);
		return new URL(urlStr);
	}

	@NonNull
	public static void write(String filePath, byte[] content) throws IOException {

		FileUtils.writeByteArrayToFile(new File(filePath), content);
	}
	
	@NonNull
	public static boolean delete(String filePath) throws IOException {

		boolean deleted = false;
		
		File file = new File(filePath);
		
		if (file.exists() && file.isFile()){
			
			deleted = file.delete();
		}
		
		return deleted;
	}
	
	@NonNull
	public static boolean exists(String filePath) {

		return (new File(filePath)).exists();
	}
	
	@NonNull
	public static boolean createDirectory(String path) {
		
		boolean created = false;
		
		File dir = new File(path);
		
		if (!dir.exists()){
			
			created = dir.mkdirs();
		}
		
		return created;
	}
	
	@NonNull
	public static boolean deleteDirectory(String path) {
		
		boolean deleted = false;
		
		File dir = new File(path);
		
		if (dir.exists() && dir.isDirectory()){
			
			deleted = dir.delete();
		}
		
		return deleted;
	}
	
	@NonNull
	public static File getFormattedFilePath(String filePath, Integer daysAgoToLoadFile, String fileFormat) {

		File file = null;

		String fileName;

		LocalDate currentDate = LocalDate.now();

		LocalDate fileDate = currentDate.minusDays(daysAgoToLoadFile);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		String formattedDate = fileDate.format(formatter);

		fileName = fileFormat.replace("%", formattedDate);

		file = new File(filePath + "/" + fileName);
		
		return file;
	}
}
