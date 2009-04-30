package org.homeunix.thecave.moss.jsp.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class ByteArrayServletOutputStream extends ServletOutputStream {

	private final ByteArrayOutputStream baos; 
	private final ServletOutputStream servletOutputStream;

	public ByteArrayServletOutputStream(ServletOutputStream servletOutputStream) {
		this.baos = new ByteArrayOutputStream();
		this.servletOutputStream = servletOutputStream;
	}
	
	@Override
	public void write(int b) throws IOException {
		baos.write(b);
		servletOutputStream.write(b);
	}
	
	public byte[] getData(){
		return baos.toByteArray();
	}
}
