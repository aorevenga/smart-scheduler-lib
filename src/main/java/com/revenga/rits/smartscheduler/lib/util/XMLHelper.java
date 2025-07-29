package com.revenga.rits.smartscheduler.lib.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class XMLHelper {
	
	private XMLHelper() {

		throw new IllegalStateException(this.getClass().getSimpleName());
	}
	
	public static ByteArrayOutputStream getXmlOutputStream(Object object, boolean includeEmptyTags) throws JAXBException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		
		StringWriter writer = new StringWriter();
		
		JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        
		Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(object, writer);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        baos.write(writer.toString().getBytes());
        
        return baos;
	}
	
	public static ByteArrayOutputStream getXmlOutputStream(FileInputStream file) throws IOException {
		
		final int DEFAULT_BUFFER_SIZE = 16384;
		InputStream is = file;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[DEFAULT_BUFFER_SIZE];

		while ((nRead = is.read(data, 0, data.length)) != -1) {
		  buffer.write(data, 0, nRead);
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        baos.write(buffer.toString().getBytes());

		return buffer;
	}
	
	public static void writeFile(InputStream inputStream, URL path) throws IOException {
		
		final int DEFAULT_BUFFER_SIZE = 16384;
		File file = new File(path.getPath());
		
		try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
			
            int read;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            
            while ((read = inputStream.read(bytes)) != -1) {
            	
                outputStream.write(bytes, 0, read);
            }
        }
	}
}
