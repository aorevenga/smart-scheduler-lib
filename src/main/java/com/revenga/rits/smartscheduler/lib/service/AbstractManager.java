package com.revenga.rits.smartscheduler.lib.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.SchedulerException;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revenga.rits.smartscheduler.lib.model.ConfigBase;
import com.revenga.rits.smartscheduler.lib.util.FileUtil;
import com.revenga.rits.smartscheduler.lib.util.ResourcesUtil;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
public abstract class AbstractManager implements LoaderInterface {
	
	private static final String MAVEN_PACKAGE = "com.revenga.rits.smartscheduler";
	private static final String MAVEN_PROP_VERSION = "version";
	
	private String mavenArtifact;

	protected ConfigBase config;
	protected ConfigBase newConfig;
	protected Class<? extends ConfigBase> configClass;
	
	protected String configJson;
	
	protected AbstractManager(String configJson, Class<? extends ConfigBase> configClass) {
		super();
		
		this.configJson = configJson;
		this.configClass = configClass;
		
		this.mavenArtifact = getMavenArtifact();
	}
	
	public void load() {
		
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			
			InputStream is = ResourcesUtil.getResource(this.configJson);
			this.config = objectMapper.readValue(is, this.configClass);
					
		} catch (IOException e) {
			
			log.error(e.getMessage());
			log.debug(ExceptionUtils.getStackTrace(e));
		}
	}
	
	public abstract void start() throws SchedulerException;
	
	public abstract void runonce() throws SchedulerException;
	
	public abstract void stop() throws SchedulerException;
	
	public ConfigBase readConfig() {
		
		return this.config;
	}
	
	public ConfigBase writeConfig(String jsonConfig) {
		
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			
			File file = ResourcesUtil.getFile(this.configJson);
					
			if (file != null) {
					
				this.newConfig = objectMapper.readValue(jsonConfig, this.configClass);
				
				String strOutput = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.newConfig);
				
				FileUtil.write(file.getPath(), strOutput.getBytes());
				
				//log.info(this.jobClass.getSimpleName() + " => New config (next exec applies): " + jsonConfig);
			}
					
		} catch (IOException e) {
			
			log.error(e.getMessage());
			log.debug(ExceptionUtils.getStackTrace(e));
		}

		return this.newConfig;
	}
	
	public String readVersion() {
		
		// Try to get version number from maven properties in jar's META-INF
		try (InputStream is = this.configClass
				.getResourceAsStream("/META-INF/maven/" + MAVEN_PACKAGE + "/" + this.mavenArtifact + "/pom.properties")) {
			if (is != null) {
				Properties p = new Properties();
				p.load(is);
				String ver = p.getProperty(MAVEN_PROP_VERSION, "").trim();
				if (!ver.isEmpty()) {
					return ver;
				}
			}
		} catch (Exception e) {
			// Ignore
		}
		
		// Try to get version number from pom.xml (available in Eclipse)
		try {
			String className = this.configClass.getName();
			String classfileName = File.separator + className.replace('.', '/') + ".class";
			URL classfileResource = this.configClass.getResource(classfileName);
			if (classfileResource != null) {
				Path absolutePackagePath = Paths.get(classfileResource.toURI()).getParent();
				int packagePathSegments = className.length() - className.replace(".", "").length();
				// Remove package segments from path, plus two more levels
				// for "target/classes", which is the standard location for
				// classes in Eclipse.
				Path path = absolutePackagePath;
				for (int i = 0, segmentsToRemove = packagePathSegments + 2; i < segmentsToRemove; i++) {
					path = path.getParent();
				}
				Path pom = path.resolve("pom.xml");
				try (InputStream is = Files.newInputStream(pom)) {
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
					doc.getDocumentElement().normalize();
					String ver = (String) XPathFactory.newInstance().newXPath().compile("/project/version")
							.evaluate(doc, XPathConstants.STRING);
					if (ver != null) {
						ver = ver.trim();
						if (!ver.isEmpty()) {
							return ver;
						}
					}
				}
			}
		} catch (Exception e) {
			// Ignore
		}

		// Fallback to using Java API to get version from MANIFEST.MF
		String ver = null;
		Package pkg = this.configClass.getPackage();
		if (pkg != null) {
			ver = pkg.getImplementationVersion();
			if (ver == null) {
				ver = pkg.getSpecificationVersion();
			}
		}
		ver = ver == null ? "" : ver.trim();
		return ver.isEmpty() ? "unknown" : ver;
	}
	
	public List<String> readLibsUsed() {
		
		return this.config.getLibs();
	}
	
	private String getMavenArtifact() {
		
		String[] classPaths = System.getProperty("java.class.path").split(":");
		
		if (classPaths.length == 1 && classPaths[0].endsWith(".jar")) {
			
			this.mavenArtifact = classPaths[0];
			int index = this.mavenArtifact.lastIndexOf(File.separator);
			
			if (index > -1) {
				
				this.mavenArtifact = this.mavenArtifact.substring(index + 1);
			}
			
			this.mavenArtifact = this.mavenArtifact.replace(".jar", "");
		}
		else {
			
			String userDir = System.getProperty("user.dir");
    		Path path = Paths.get(userDir);
    		this.mavenArtifact = path.getFileName().toString();
		}
		
        return this.mavenArtifact;
	}
}
