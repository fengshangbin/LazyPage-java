package com.c3.lazypage.analyze;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import com.c3.lazypage.LazyPage;

public class LazyScriptEngine {
	private static CompiledScript compiled;
	static{
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("nashorn");
		StringBuffer jsStringBuffer = new StringBuffer();
		LazyPage.jsPaths.forEach(jsPath -> {
			jsStringBuffer.append(readToString(jsPath));
		});
		InputStream is=LazyScriptEngine.class.getResourceAsStream("/baiduTemplate.js");
        BufferedReader br=new BufferedReader(new InputStreamReader(is));
		//String jsFileName = "D:\\myEclipseWorkSpace\\J2EEWebTest\\src\\baiduTemplate.js";
		try {
			//FileReader reader = new FileReader(jsFileName);
			
			String line = br.readLine();
			while (line != null) {
				jsStringBuffer.append(line).append("\r\n");
				line = br.readLine();
			}
			br.close();
			
			compiled = ((Compilable)engine).compile(jsStringBuffer.toString());
			//reader.close();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String run(String str, String data, String mode){
		Bindings bindings = new SimpleBindings();
		bindings.put("str", str);
		bindings.put("data", data);
		bindings.put("mode", mode);
        String result = "";
        try {
        	result = (String)compiled.eval(bindings);
        } catch (Exception e) {
        	//System.out.println(str+"-"+data+"-"+modeData);
			e.printStackTrace();
		}
        return result;
	}
	
	public static String readToString(String fileName) {
        File file = new File(fileName);  
        Long filelength = file.length();  
        byte[] filecontent = new byte[filelength.intValue()];  
        try {  
            FileInputStream in = new FileInputStream(file);  
            in.read(filecontent);  
            in.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        try {  
            return new String(filecontent, LazyPage.encoding);  
        } catch (UnsupportedEncodingException e) {
        	String errorMessage = "The OS does not support " + LazyPage.encoding;
            System.err.println(errorMessage);
            e.printStackTrace();  
            return errorMessage;  
        }  
    }
}
