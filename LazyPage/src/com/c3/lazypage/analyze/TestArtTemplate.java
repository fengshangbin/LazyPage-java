package com.c3.lazypage.analyze;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class TestArtTemplate {
	
	private static ScriptEngineManager manager = new ScriptEngineManager();
	private static ScriptEngine engine = manager.getEngineByName("nashorn");
	
	private static void testCompiled(){
		CompiledScript compiled = null;
		StringBuffer jsStringBuffer = new StringBuffer();
		jsStringBuffer.append("var process = {'env': {'NODE_ENV': 'production'}};"); //development production
		InputStream is=TestArtTemplate.class.getResourceAsStream("/template-web.js");
		loadJSCode(is, jsStringBuffer);
		jsStringBuffer.append("function run(html, data) {if (typeof data == 'string') data = JSON.parse(data); result = template.render(html, data); return result;};");
		jsStringBuffer.append("run(html, data)");
        try {
			compiled = ((Compilable)engine).compile(jsStringBuffer.toString());
		} catch (ScriptException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int len = 10;
		int total = 0;
		int max = 0;
		int min = 1000000;
		for(int i=0; i<len; i++){
			long start = System.currentTimeMillis();
			Bindings bindings = new SimpleBindings();
			bindings.put("html", "123<%= name %>456");
			bindings.put("data", "{\"name\":\"hi\"}");
	        String result = "";
	        try {
	        	result = (String)compiled.eval(bindings);
	        } catch (Exception e) {
				e.printStackTrace();
			}
	        int time = (int)(System.currentTimeMillis()-start);
	        total += time;
	        if(max<time)max=time;
	        else if(min>time)min=time;
	        //System.out.println(result);
		}
		System.out.println((total-max-min)/(len-2));
	}
	
	private static void testInvokeFunction(){
		Invocable invokeEngine = null;
		StringBuffer jsStringBuffer = new StringBuffer();
		jsStringBuffer.append("var process = {'env': {'NODE_ENV': 'production'}};");
		InputStream is=TestArtTemplate.class.getResourceAsStream("/template-web.js");
		loadJSCode(is, jsStringBuffer);
		jsStringBuffer.append("function run(html, data) {if (typeof data == 'string') data = JSON.parse(data); result = template.render(html, data); return result;};");
        try {
        	engine.eval(jsStringBuffer.toString());
			invokeEngine = (Invocable)engine;
		} catch (ScriptException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int len = 10;
		int total = 0;
		int max = 0;
		int min = 1000000;
		for(int i=0; i<len; i++){
			long start = System.currentTimeMillis();
	        String result = "";
	        try {
	        	result = (String)invokeEngine.invokeFunction("run", "123<%= name %>456", "{\"name\":\"hi\"}");
	        } catch (Exception e) {
				e.printStackTrace();
			}
	        int time = (int)(System.currentTimeMillis()-start);
	        total += time;
	        if(max<time)max=time;
	        else if(min>time)min=time;
	        //System.out.println(result);
		}
		System.out.println((total-max-min)/(len-2)); //26
	}
	
	private static void testCompiledInvokeFunction(){
		CompiledScript compiled = null;
		Invocable invocable = null;
		StringBuffer jsStringBuffer = new StringBuffer();
		jsStringBuffer.append("var process = {'env': {'NODE_ENV': 'production'}};");
		InputStream is=TestArtTemplate.class.getResourceAsStream("/template-web.js");
		loadJSCode(is, jsStringBuffer);
		jsStringBuffer.append("function run(html, data) {if (typeof data == 'string') data = JSON.parse(data); result = template.render(html, data); return result;};");
        try {
			compiled = ((Compilable)engine).compile(jsStringBuffer.toString());
			compiled.eval();
		} catch (ScriptException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		invocable = (Invocable) compiled.getEngine();
		int len = 10;
		int total = 0;
		int max = 0;
		int min = 1000000;
		for(int i=0; i<len; i++){
			long start = System.currentTimeMillis();
	        String result = "";
	        try {
	        	result = (String)invocable.invokeFunction("run", "123<%= name %>456", "{\"name\":\"hi\"}");
	        } catch (Exception e) {
				e.printStackTrace();
			}
	        int time = (int)(System.currentTimeMillis()-start);
	        total += time;
	        if(max<time)max=time;
	        else if(min>time)min=time;
	        //System.out.println(result);
		}
		System.out.println((total-max-min)/(len-2));
	}
	
	private static void testFormat(){
		CompiledScript compiled = null;
		Invocable invocable = null;
		StringBuffer jsStringBuffer = new StringBuffer();
		
		jsStringBuffer.append("var process = {'env': {'NODE_ENV': 'production'}};");
		InputStream is=TestArtTemplate.class.getResourceAsStream("/template-web.js");
		loadJSCode(is, jsStringBuffer);
		jsStringBuffer.append("template.defaults.imports.$import = {};");
		jsStringBuffer.append("function run(html, data, arr) {"
				//+ "if (typeof data == 'string') data = JSON.parse(data); "
				+ "result = template.render(html, data); "
				+ "return result+(arr[0]+arr[1]);};");
		
		jsStringBuffer.append("(function(root) {");
		InputStream is2=TestArtTemplate.class.getResourceAsStream("/format.js");
		loadJSCode(is2, jsStringBuffer);
		jsStringBuffer.append("})(template.defaults.imports.$import);");
		
		jsStringBuffer.append("template.defaults.imports.$config = "+"{\"price\":999999999}");
		
        try {
			compiled = ((Compilable)engine).compile(jsStringBuffer.toString());
			compiled.eval();
		} catch (ScriptException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		invocable = (Invocable) compiled.getEngine();
		int len = 10;
		int total = 0;
		int max = 0;
		int min = 1000000;
		for(int i=0; i<len; i++){
			long start = System.currentTimeMillis();
	        String result = "";
	        try {
	        	/*Object obj = ((ScriptEngine)invocable).get("format");
	        	result = (String)invocable.invokeMethod(obj, "toThousands", 999999);*/
	        	HashMap map = new HashMap();
	        	map.put("price", 999999999);//"{\"price\":999999999}"
	        	HashMap book = new HashMap();
	        	book.put("name", "hello");
	        	map.put("book", book);
	        	result = (String)invocable.invokeFunction("run", "123{{ $import.format.toThousands(price) }}45{{ book.name }}6{{ $config.price }}", map, new int[]{1,5});
	        } catch (Exception e) {
				e.printStackTrace();
			}
	        int time = (int)(System.currentTimeMillis()-start);
	        total += time;
	        if(max<time)max=time;
	        else if(min>time)min=time;
	        System.out.println(result);
		}
		System.out.println((total-max-min)/(len-2));
	}
	
	private static void loadJSCode(String fileName, StringBuffer sb){
		try {
			loadJSCode(new FileInputStream(fileName), sb);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void loadJSCode(File file, StringBuffer sb){
		try {
			loadJSCode(new FileInputStream(file), sb);
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void loadJSCode(InputStream is, StringBuffer sb){
		try {
			loadJSCode(new InputStreamReader(is, "UTF-8"), sb);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void loadJSCode(InputStreamReader reader, StringBuffer sb){
		BufferedReader br=new BufferedReader(reader);
		try {
			String line = br.readLine();
			while (line != null) {
				sb.append(line).append("\r\n");
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws ScriptException, NoSuchMethodException {
		// TODO Auto-generated method stub
		//testCompiled(); //27
		//testInvokeFunction(); //6
		//testCompiledInvokeFunction(); //6
		testFormat();
	}
}
