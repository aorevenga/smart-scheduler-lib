package com.revenga.rits.smartscheduler.lib.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ConversionUtil {

	private ConversionUtil() {
		
		throw new IllegalStateException(this.getClass().getSimpleName());
	}
	
	public static Map<String, Object> clone(Map<String, Object> original) throws JsonProcessingException {
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		return objectMapper.readValue(objectMapper.writeValueAsString(original), new TypeReference<Map<String, Object>>() {});
	}

	public static JsonNode mapToJson(Map<String, Object> map) {

		return (new ObjectMapper()).valueToTree(map);
	}

	public static String mapToString(Map<String, Object> map) {

		return mapToJson(map).toString();
	}

	public static Map<String, Object> stringToMap(String jsonStr) throws JsonProcessingException {

		return (new ObjectMapper()).readValue(jsonStr, new TypeReference<HashMap<String, Object>>() {});
	}
	
	public static List<Map<String, Object>> stringToListMap(String jsonStr) throws JsonProcessingException {
		
		ObjectMapper mapper = new ObjectMapper();
		
		return mapper.readValue(jsonStr, mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
	}

	public static Map<String, Object> jsonToMap(JsonNode jsonNode) throws JsonProcessingException {

		return stringToMap(jsonNode.toString());
	}
	
	public static JsonNode stringToJson(String jsonStr) throws JsonProcessingException {
		
		return (new ObjectMapper()).readTree(jsonStr);
	}
	
	public static JsonNode mapToJson(String name, Map<String, Object> map) throws JsonProcessingException {
		
		ObjectMapper mapper = new ObjectMapper();
		
		JsonNode root = mapper.readTree(String.format("{\"%s\": null}", name));
		
		((ObjectNode) root).set(name, mapToJson(map));
		
		return root;
	}
	
	public static boolean isValidJSON(final String json) {
	    
		boolean valid = true;
	    
	    try {
	    	
	        (new ObjectMapper()).readTree(json);
	        
	    } catch(JsonProcessingException e){
	    	
	        valid = false;
	    }
	    
	    return valid;
	}
	
	public static Map<String, Object> objectToMap(Object obj) {
		
		ObjectMapper mapper = new ObjectMapper();

		return mapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
	}
	
	public static Object mapToObject(Class<?> clazz, Map<String, Object> map) {
		
		ObjectMapper mapper = new ObjectMapper();
		
		return mapper.convertValue(map, clazz);
	}
}
