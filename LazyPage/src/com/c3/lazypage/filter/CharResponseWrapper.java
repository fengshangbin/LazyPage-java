package com.c3.lazypage.filter;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CharResponseWrapper extends HttpServletResponseWrapper {
	private CharArrayWriter output;

	public CharResponseWrapper(HttpServletResponse response) {
		super(response);
		output = new CharArrayWriter();
	}

	public String getResponseContent() {
		return output.toString();
	}

	public PrintWriter getWriter() {
		return new PrintWriter(output);
	}
}
