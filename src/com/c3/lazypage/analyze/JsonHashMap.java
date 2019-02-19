package com.c3.lazypage.analyze;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonHashMap<K,V> extends HashMap<K, V> {
	private static final long serialVersionUID = 362498820763181265L;
	
	public String toString(){
		Iterator<Entry<K,V>> i = entrySet().iterator();
        if (! i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Entry<K,V> e = i.next();
            K key = e.getKey();
            V value = e.getValue();
            boolean isString = value instanceof String;
            if(isString){
            	String matchJson = "(^\\{([\\S\\s]*?)\\}$)|(^\\[([\\S\\s]*?)\\]$)";
        		Pattern pattern = Pattern.compile(matchJson);
        		Matcher m = pattern.matcher((String)value);
    			if(m.find())isString=false;
            }
            sb.append("\"");
            sb.append(key);
            sb.append("\"");
            sb.append(":");
            if(isString)sb.append("\"");
            sb.append(value);
            if(isString)sb.append("\"");
            if (! i.hasNext())
                return sb.append("}").toString();
            sb.append(";").append(" ");
        }
	}
	public String toString(String exportKey){
		Iterator<Entry<K,V>> i = entrySet().iterator();
        if (! i.hasNext())
            return "";
        StringBuilder sb = new StringBuilder();
        for (;;) {
            Entry<K,V> e = i.next();
            K key = e.getKey();
            V value = e.getValue();
            boolean isString = value instanceof String;
            if(isString){
            	String matchJson = "(^\\{([\\S\\s]*?)\\}$)|(^\\[([\\S\\s]*?)\\]$)";
        		Pattern pattern = Pattern.compile(matchJson);
        		Matcher m = pattern.matcher((String)value);
    			if(m.find())isString=false;
            }
            sb.append(exportKey);
            sb.append("[\"");
            sb.append(key);
            sb.append("\"]");
            sb.append("=");
            if(isString)sb.append("\"");
            sb.append(value);
            if(isString)sb.append("\"");
            if (! i.hasNext())
                return sb.append(";").toString();
            sb.append(";").append(" ");
        }
	}
}
