package com.c3.lazypage.analyze;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;

import com.c3.lazypage.LazyPage;
import com.c3.lazypage.entity.Block;
import com.c3.lazypage.entity.Document;
import com.c3.lazypage.servlet.LazyPageServlet;

public class AnalyzeHtml {
	private JsonHashMap<String, String> dataMap = new JsonHashMap<String, String>();
	private Document doc;
	private boolean continueCheck=false;
	private String rootPath;
	private String[] paths;
	private String query;
	private String[] pathParams;
	private Cookie[] cookies;
	public AnalyzeHtml(){
		//doc = new Document(html);
	}
	public static void main(String[] args) {
		/*String html = LazyPageServlet.readToString("D:\\js project tools\\lazypage\\examples\\index-.html");
		AnalyzeHtml analyzeHtml = new AnalyzeHtml();
		String outHtml = analyzeHtml.parse("http://localhost:8080/J2EEWebTest/index.html", "id=20", html, null, null);
		System.out.println(outHtml);*/
		String name="q";
		String reg = "(^|&)"+ name +"=([^&]*)(&|$)";
		Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher("q=city");
		if(m.find()){
			System.out.println(m.group(2));
		}
	}
	public String parse(String path, String query, String html, String[] pathParams, Cookie[] cookies){
		doc = new Document(html);
		this.rootPath = getRootPath(path);
		path = path.replaceAll("("+rootPath+"/?)", "");
		if(LazyPage.host!=null)this.rootPath=LazyPage.host;
		if(path.endsWith("/"))path+="end";
		this.paths = path.split("/");
		//this.paths = path.replaceAll("("+rootPath+"/?)", "");
		if(paths.length>0){
			paths = Arrays.copyOf(paths, paths.length-1);
		}
		this.query = query;
		this.pathParams = pathParams;
		this.cookies = cookies;
		checkBlocks(doc);
		String result = doc.getHtml().replaceAll("x-tmpl-lazypage-tag", "x-tmpl-lazypage");
		if(!dataMap.isEmpty()){
			int bodyEnd = result.lastIndexOf("</body>");
			if(bodyEnd>0){
				result = result.substring(0, bodyEnd)+"<script>"+dataMap.toString("LazyPage.data")+"</script>\n"+result.substring(bodyEnd);
			}else{
				result += "\n<script>"+dataMap.toString("LazyPage.data")+"</script>";
			}
		}
		return result;
	}
	private static String getRootPath(String path){
		String regex = "^((https|http|ftp|rtsp|mms)?://[^/]*)";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher(path);
		if(m.find()){
			return m.group(1);
		}
		return null;
	}
	private void checkBlocks(Document doc){
		continueCheck=false;
		Vector<Block> blocks = doc.queryBlocks();
		//int lazyCount = 0;
		for(int i=0; i<blocks.size(); i++){
			Block block = blocks.get(i);
			if(block.getRunStated() > 0)continue;
			String lazyStr = block.getAttribute("lazy");
			if(lazyStr!=null && !lazyStr.equals("false")){
				//lazyCount++;
				continue;
			}
			String waitStr = block.getAttribute("wait");
			//System.out.println("+++--"+block.getAttrHtml()+"+++++"+waitStr+".");
			if(waitStr!=null && !waitStr.isEmpty()){
				String[] waits = waitStr.split(" ");
				boolean removeWait = false;
				for(int j=0; j<waits.length; j++){
					String waitID = waits[j];
					if(dataMap.containsKey(waitID)){
						waitStr = waitStr.replaceAll(waitID+" ?", "");
						removeWait = true;
					}
				}
				if(removeWait == true)
					block.setAttribute("wait", waitStr);
			}
			if(waitStr!=null && !waitStr.isEmpty()){
				//lazyCount++;
				continue;
			}
			//System.out.println("+++"+block.getAttrHtml());
			runBlock(block);
		}
		//System.out.println("continueCheck: "+continueCheck);
		if(continueCheck == true)checkBlocks(doc);
	}
	private void addModeData(Block block, String data){
		if(data==null)return;
		block.setData(data);
		String id = block.getAttribute("id");
		if(id!=null){
			dataMap.put(id, data);
			List<Block> blocks = doc.getBlocks().stream()
					.filter(item -> item.hasAttribute("wait"))
					.collect(Collectors.toList());
			for(int i=0; i<blocks.size(); i++){
				Block item = blocks.get(i);
				String wait = item.getAttribute("wait");
				wait = wait.replaceAll(id+" ?", "");
				item.setAttribute("wait", wait);
			}
		}
	}
	private String getQueryString(String name){
		if(query==null)return "";
		String reg = "(^|&)"+ name +"=([^&]*)(&|$)";
		Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher(query);
		if(m.find()){
			return m.group(2);
		}
		return "";
	}
	private String replaceQuery(String str, boolean isString){
		if(str==null)return null;
		String regStr = "\\{&(.*?)\\}";
		Pattern pattern = Pattern.compile(regStr);
		Matcher m = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String key = m.group(1);
			String value = getQueryString(key);
			if(!isString)value = '"' + value + '"';
			m.appendReplacement(sb, value);
		}
		m.appendTail(sb);
		return sb.toString();
	}
	private String replaceModeData(String str, boolean isString){
		if(str==null)return null;
		String regStr = "\\{@(.*?)\\}";
		Pattern pattern = Pattern.compile(regStr);
		Matcher m = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			//System.out.println(m.group(1));
			//System.out.println(dataMap.toString());
			String value = LazyScriptEngine.run(m.group(1), dataMap.toString(), "\""+isString+"\"");
			m.appendReplacement(sb, value);
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private String getPathString(String index){
		if(pathParams==null)return "";
		int i = Integer.parseInt(index);
		if(i>=0 && i<pathParams.length){
			return pathParams[i];
		}
		return "";
	}
	private String replacePath(String str, boolean isString){
		if(str==null)return null;
		String regStr = "\\{\\$(.*?)\\}";
		Pattern pattern = Pattern.compile(regStr);
		Matcher m = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String key = m.group(1);
			String value = getPathString(key);
			if(!isString)value = '"' + value + '"';
			m.appendReplacement(sb, value);
		}
		m.appendTail(sb);
		return sb.toString();
	}
	private String replaceParamAsValue(String html, boolean isString) {
		html=replaceQuery(html, isString);
		html=replacePath(html, isString);
		html=replaceModeData(html, isString);
	    return html;
	}
	private void runBlock(Block block){
		block.setRunStated(1);
		String src = block.getAttribute("src");
		String source = block.getAttribute("source");
		String matchJson = "(^\\{(.*?)\\}$)|(^\\[(.*?)\\]$)";
		Pattern pattern = Pattern.compile(matchJson);
		if(source!=null&&source!=""){
			Matcher m = pattern.matcher(source);
			if(m.find()){
				source=source.replaceAll("'","\"");
				addModeData(block, source);
			}else{
				String ajaxType = block.getAttribute("ajax-type");
				String ajaxData = block.getAttribute("ajax-data");
				ajaxData = replaceParamAsValue(ajaxData, true);
				source = replaceParamAsValue(source, true);
				String result = LazyHttpProxy.ajax(rootPath, paths, ajaxType, source, ajaxData, cookies);
				addModeData(block, result);
				//renderDom(block);
			}
		}else{
			addModeData(block, "{}");
		}
		if(src!=null&&src!=""){
			src = replaceParamAsValue(src, true);
			String result = LazyHttpProxy.ajax(rootPath, paths, null, src, null, cookies);
			block.setHtml(result);
			renderDom(block);
		}else{
			String html = block.getInnerHtml();
			html = html.replaceAll("jscript", "script");
			block.setHtml(html);
			renderDom(block);
		}
	}
	private void renderDom(Block block){
		String html = block.getHtml();
		String data = block.getData();
		if(html==null||data==null)return;
		//System.out.println(data);
		//System.out.println(dataMap.toString());
		html = replaceParamAsValue(html, false);
		String out = LazyScriptEngine.run(html, data, null);
		//System.out.println("---"+block.getAttrHtml()+"------"+out);
		block.setOutHtml(out);
		//block=null;
		//checkBlocks();
		continueCheck = true;
	}
}
