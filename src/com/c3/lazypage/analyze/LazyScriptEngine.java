package com.c3.lazypage.analyze;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import com.c3.lazypage.LazyPage;
import com.c3.lazypage.servlet.LazyPageServlet;

public class LazyScriptEngine {
	private static CompiledScript compiled;
	static{
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("nashorn");
		StringBuffer jsStringBuffer = new StringBuffer();
		LazyPage.jsPaths.forEach(jsPath -> {
			jsStringBuffer.append(LazyPageServlet.readToString(jsPath));
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
	public static String run(String str, String data, String modeData){
		Bindings bindings = new SimpleBindings();
		bindings.put("str", str);
		bindings.put("data", data);
		bindings.put("modeData", modeData);
        String result = "";
        try {
        	result = (String)compiled.eval(bindings);
        } catch (Exception e) {
        	//System.out.println(str+"-"+data+"-"+modeData);
			e.printStackTrace();
		}
        return result;
	}
}
