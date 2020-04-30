HttpServletRequest request = (HttpServletRequest) req;
	String serverPath = request.getServletPath().toLowerCase();
	String fileName = serverPath.substring(serverPath.lastIndexOf("/")+1);
	
	//request.getHeader(arg0);
	if(fileName.indexOf(".")>0){
		if(!fileName.endsWith(".html")){
			chain.doFilter(req, resp);
			return;
		}else if(fileName.startsWith("_")){
			chain.doFilter(req, resp);
			return;
		}
	}
	//System.out.println("serverPath:"+serverPath);
	//LOG.info("serverPath:"+serverPath);
	if(serverPath.endsWith("/") && LazyPage.htmlPaths.contains(serverPath+"index.html")){
		String query = request.getQueryString();
    	String url = request.getRequestURL().toString();
		request.setAttribute("lazypage_url", url);
		request.setAttribute("lazypage_query", query);
		request.getRequestDispatcher(serverPath+"index.html").forward(req, resp);
		return;
	}
	System.out.println(serverPath +"|"+ fileName+"|"+LazyPage.htmlPaths.contains(serverPath));
	if(! LazyPage.htmlPaths.contains(serverPath)){
		for (Entry<String, String> e: LazyPage.map) {
			//System.out.println(e.getKey()+":"+e.getValue());
			String route = e.getKey();
			Pattern pattern = Pattern.compile(route, Pattern.CASE_INSENSITIVE);
			Matcher isUrl = pattern.matcher(serverPath);
			if(isUrl.matches()){
				/*int count = isUrl.groupCount();
				String[] group = null;
				if(count>0){
					group = new String[count];
					for(int i=0; i<count; i++){
						group[i]=isUrl.group(i+1);
					}
				}
				if(group != null){
					request.setAttribute("lazypage_group", group);
					//request.setAttribute("lazypage_route", route);
				}*/
				String realPath = e.getValue().replaceAll("\\+", "%2B");
				//String realPath = URLDecoder.decode(e.getValue(), "utf-8");
				String query = request.getQueryString();
		    	String url = request.getRequestURL().toString();
				request.setAttribute("lazypage_url", url);
				request.setAttribute("lazypage_query", query);
				request.getRequestDispatcher(realPath).forward(req, resp);
				return;
			}
		}
		chain.doFilter(req, resp);
		return;
	}
	
	HttpServletResponse response = (HttpServletResponse) resp;
	/*ResponseWrapper respWrapper = new ResponseWrapper(response);
	chain.doFilter(req, respWrapper);
	byte[] content = respWrapper.getContent();
	System.out.println(content.length);*/
	CharResponseWrapper wrapper = new CharResponseWrapper(response);
    chain.doFilter(request, wrapper);
    String originalContent = wrapper.getResponseContent();
    System.out.println("."+originalContent+".");
    /*response.setContentLength(outByte.length);
	ServletOutputStream out = response.getOutputStream();
    out.write(outByte);
    out.flush();*/
    
    byte[] outByte = "Added Title".getBytes("UTF-8");
	response.setCharacterEncoding("UTF-8");
	response.setContentType("text/html");
	response.setContentLength(outByte.length);
	ServletOutputStream out = response.getOutputStream();
    out.write(outByte);
    out.flush();
    System.out.println("."+outByte.length+".");
    
    /*CharArrayWriter writer = new CharArrayWriter();
    writer.write("Added Title");
    response.setContentLength(writer.toString().length()); 
    PrintWriter out = response.getWriter();
    out.write(writer.toString());
    out.flush();*/
    
	//if (content.length > 0) {
    if(originalContent.length()>0 && false){
		String outString = originalContent; //new String(content, "UTF-8");
		//boolean lazyPageSpider = false;
		Cookie[] cookies = request.getCookies();
		/*if(cookies!=null){
	    	for (Cookie cookie : cookies) {
	    		if(cookie.getName().equals("LazyPageSpider")){
	    			lazyPageSpider = true;
		 			break;
	    		}
	    	}
    	}*/

		/*Object lazypageGroup = request.getAttribute("lazypage_group");
		String[] pathParams = null;
		if(lazypageGroup!=null){
			pathParams = (String[])lazypageGroup;
			//String lazypageRoute = (String)request.getAttribute("lazypage_route");
			if(pathParams!=null){
				String pathStr = "[\""+String.join("\",\"", pathParams)+"\"]";
				int bodyEnd = outString.lastIndexOf("</body>");
				if(bodyEnd>0){
					outString = outString.substring(0, bodyEnd)+"<script>LazyPage.pathParams="+pathStr+";</script>\n"+outString.substring(bodyEnd);
				}else{
					outString += "\n<script>LazyPage.pathParams="+pathStr+"</script>";
				}
				//LazyPage.pathReg='"+lazypageRoute+"';
			}
		}*/
		
		//if(lazyPageSpider == false){
		Object lazypageQuery = request.getAttribute("lazypage_query");
		String query = lazypageQuery!=null?lazypageQuery.toString():request.getQueryString();
		if(query!=null)query=URLDecoder.decode(query, "utf-8");
		//System.out.println("query:"+query);
		Object lazypageUrl = request.getAttribute("lazypage_url");
    	String url = lazypageUrl!=null?lazypageUrl.toString():request.getRequestURL().toString();
    	System.out.println(url +"|"+ query +"|"+ outString);
		FastDom dom = new AnalyzeHtml().parse(url, query, outString, cookies);
		//}
		
		String lazypageTargetSelector = getQueryString(query, "lazypageTargetSelector");
		if(lazypageTargetSelector.length()>0){
			Element block = QueryLazyPage.queryLazyPageSelector(dom, lazypageTargetSelector);
			
			JsonHashMap<String, Object> dataMap = new JsonHashMap<String, Object>();
			dataMap.put("block", block!=null ? block.getOuterHTML().replaceAll(" lazypagelevel\\d", "") : null);
			dataMap.put("hasTargetLazyPage", block != null);
            if (block!=null) {
        	    dataMap.put("title", dom.querySelector("title").getInnerHTML());
            }
          outString = dataMap.toString();
          //System.out.print(outString);
		}else{
			outString = dom.getHTML().replaceAll("(\r|\n)( *(\r|\n))+", "\r");
		}
		
		/*byte[] outByte = outString.getBytes("UTF-8");
		response.setCharacterEncoding("UTF-8");
    	response.setContentType("text/html");
		response.setContentLength(outByte.length);
		ServletOutputStream out = response.getOutputStream();
        out.write(outByte);
        out.flush();*/

        /*PrintWriter responseWriter = response.getWriter();
        responseWriter.write(outString);*/
	}