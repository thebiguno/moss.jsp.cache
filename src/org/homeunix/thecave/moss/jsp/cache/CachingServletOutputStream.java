package org.homeunix.thecave.moss.jsp.cache;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

public class CachingServletOutputStream extends ServletOutputStream {
 
	private final OutputStream outputStream;

	public CachingServletOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	@Override
	public void write(int b) throws IOException {
		outputStream.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		outputStream.write(b, off, len);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		outputStream.write(b);
	}
}
