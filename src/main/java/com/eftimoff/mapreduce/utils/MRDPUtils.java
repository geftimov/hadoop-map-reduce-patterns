package com.eftimoff.mapreduce.utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

public class MRDPUtils {
	private static final Pattern INTEGER = Pattern.compile("\\d+");

	  public static Map<String, String> transformXmlToMap(String xml) {
	    Map<String,String> map = new HashMap<String,String>();
	    try {
	      String[] tokens = xml.trim().substring(5, xml.trim().length() - 3).split("\"");
	      for (int i = 0; i < tokens.length; i += 2) {
	        String key = tokens[i].trim();
	        String val;
	        if (i + 1 >= tokens.length) {
	          val = null;
	        } else {
	          val = StringEscapeUtils.unescapeHtml(tokens[i+1].trim());
	        }

	        map.put(key.substring(0, key.length() - 1), val);
	      }
	    } catch (StringIndexOutOfBoundsException e) {
	      System.err.println(xml);
	    }

	    return map;
	  }

	  public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");

	  public static boolean isNullOrEmpty(String str) {
	    return str == null || str.length() == 0;
	  }

	  public static boolean isInteger(String str) {
	    Matcher is = INTEGER.matcher(str);
	    return is.matches();
	  }
}
