package org.homeunix.thecave.moss.jsp.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ByteServletResponseWrapper extends HttpServletResponseWrapper {

	private final ByteArrayOutputStream baos;

	public ByteServletResponseWrapper(HttpServletResponse response){
		super(response);
		baos = new ByteArrayOutputStream();
	}
	
	@Override
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(baos);
	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return new ByteArrayServletOutputStream(baos);
	}
	
	public byte[] getResponseBytes(){
		return baos.toByteArray();
	}
}
