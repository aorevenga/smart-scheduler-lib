package com.revenga.rits.smartscheduler.lib.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.springframework.util.CollectionUtils;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.pdf.BaseFont;

public class PDFHelper {

	private PDFHelper() {

		throw new IllegalStateException(this.getClass().getSimpleName());
	}
	
	public static Document getDocument(String filePath) throws IOException {
		
		Document doc = null;
		
		if (FileUtil.exists(filePath)) {
			
			doc = Jsoup.parse(new File(filePath), StandardCharsets.UTF_8.name());
		}
		
		return doc;
	}
	
	public static Document replaceHtmlTemplate(String inputHTML, Map<String, String> props) {
		
		Document document = Jsoup.parse(inputHTML, StandardCharsets.UTF_8.name());
		String outputHTML = document.toString();
		
		if (!CollectionUtils.isEmpty(props)) {
			
			for (Map.Entry<String, String> prop : props.entrySet()) {
				
				String name = prop.getKey();
				String value = prop.getValue();
				
				outputHTML = outputHTML.replace("{{" + name + "}}", !StringUtils.isEmpty(value) ? value : "---");
			}
		}
		
		return Jsoup.parse(outputHTML, StandardCharsets.UTF_8.name());
	}
		
	public static File convertHtml2Pdf(String inputHtml, String outputPdf) throws IOException {
		
		String html = FileUtils.readFileToString(new File(inputHtml), StandardCharsets.UTF_8);
		
		Document document = Jsoup.parse(html, "UTF-8");
		document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
		
		html = Parser.unescapeEntities(document.toString(), false); 
		
		FileUtils.write(new File(inputHtml), html, StandardCharsets.UTF_8);
		
		try (OutputStream outputStream = new FileOutputStream(outputPdf)) {
			
		    ITextRenderer renderer = new ITextRenderer();
		    SharedContext sharedContext = renderer.getSharedContext();
		    sharedContext.setBaseURL(inputHtml);
		    sharedContext.setPrint(true);
		    sharedContext.setInteractive(false);
		    renderer.setDocument(new File(inputHtml));
		    
		    URL urlFonts = FileUtil.getUrl("$SMART_SCHEDULER_PATH/fonts");
		    
		    if (FileUtil.exists(urlFonts.getPath())) {
		    
		    	Collection<File> fonts = FileUtils.listFiles(new File(urlFonts.getPath()), new String[] {"ttf"}, false);
			    
			    if (!CollectionUtils.isEmpty(fonts)) {
			    	
			    	ITextFontResolver fontResolver = renderer.getFontResolver();
			    	
			    	for (File font : fonts) {
						
			    		fontResolver.addFont(font.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
					}
			    }
		    }
		    
		    renderer.layout();
		    renderer.createPDF(outputStream);
		}
		
		return new File(outputPdf);
	}
}
