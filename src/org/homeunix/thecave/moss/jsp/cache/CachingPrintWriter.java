package org.homeunix.thecave.moss.jsp.cache;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

public class CachingPrintWriter extends PrintWriter {

	private final ByteArrayOutputStream baos; 
	private final PrintWriter printWriter;
	private final PrintWriter bufferedPrintWriter;

	public CachingPrintWriter(PrintWriter printWriter) {
		super(printWriter);
		this.baos = new ByteArrayOutputStream();
		this.printWriter = printWriter;
		this.bufferedPrintWriter = new PrintWriter(baos);
	}
	
	@Override
	public void write(int c) {
		printWriter.write(c);
		bufferedPrintWriter.write(c);
	}
	
	@Override
	public void write(char[] buf) {
		printWriter.write(buf);
		bufferedPrintWriter.write(buf);
	}
	
	@Override
	public void write(char[] buf, int off, int len) {
		printWriter.write(buf, off, len);
		bufferedPrintWriter.write(buf, off, len);
	}
	
	@Override
	public void write(String s) {
		printWriter.write(s);
		bufferedPrintWriter.write(s);
	}
	
	@Override
	public void write(String s, int off, int len) {
		printWriter.write(s, off, len);
		bufferedPrintWriter.write(s, off, len);
	}
	
	public byte[] getData(){
		bufferedPrintWriter.flush();
		return baos.toByteArray();
	}
}
