package org.homeunix.thecave.moss.jsp.cache.io;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

public class CachingServletOutputStream extends ServletOutputStream {
 
	private final OutputStream outputStream;
	private final OutputStream cachedOutputStream;

	public CachingServletOutputStream(OutputStream outputStream, OutputStream cachedOutputStream) {
		this.outputStream = outputStream;
		this.cachedOutputStream = cachedOutputStream;
	}
	
	@Override
	public void write(int b) throws IOException {
		outputStream.write(b);
		cachedOutputStream.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		outputStream.write(b, off, len);
		cachedOutputStream.write(b, off, len);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		outputStream.write(b);
		cachedOutputStream.write(b);
	}
	
	@Override
	public void flush() throws IOException {
		super.flush();
		outputStream.flush();
		cachedOutputStream.flush();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		outputStream.close();
		cachedOutputStream.close();
	}
}
