# LazyPage-java
LazyPage server jave version 
# 关于LazyPage
参见 https://github.com/fengshangbin/LazyPage
# 如何使用LazyPage-java
1，拷贝lazypage.jar到你的项目  
2，在项目初始化时 执行LazyPage初始化 LazyPage.init(context)  
### LazyPage方法说明
/**
 * LazyPage初始化方法，请在web初始化时调用如ServletContextListener实例的contextInitialized中
 * @param  context  Servlet上下文Context
 * @param  htmlPath  html文件所在目录，默认为web跟目录
 * @param  scanChildrenDirectory  是否扫描子目录中的html文件，默认为false
 */
void init(ServletContext context, String htmlPath, boolean scanChildrenDirectory)

/**
 * 注册全局脚本文件，请在web初始化时调用
 * @param  jsPath  脚本文件路径
 */
void addJsFile(String jsPath)