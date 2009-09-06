package ca.digitalcave.moss.jsp.cache.io;

import java.io.PrintWriter;

public class CachingPrintWriter extends PrintWriter {

	private final PrintWriter printWriter;
	private final PrintWriter cachedPrintWriter;

	public CachingPrintWriter(PrintWriter printWriter, PrintWriter cachedPrintWriter) {
		super(printWriter);
		this.printWriter = printWriter;
		this.cachedPrintWriter = cachedPrintWriter;
	}
	
	@Override
	public void write(int c) {
		printWriter.write(c);
		cachedPrintWriter.write(c);
	}
	
	@Override
	public void write(char[] buf) {
		printWriter.write(buf);
		cachedPrintWriter.write(buf);
	}
	
	@Override
	public void write(char[] buf, int off, int len) {
		printWriter.write(buf, off, len);
		cachedPrintWriter.write(buf, off, len);
	}
	
	@Override
	public void write(String s) {
		printWriter.write(s);
		cachedPrintWriter.write(s);
	}
	
	@Override
	public void write(String s, int off, int len) {
		printWriter.write(s, off, len);
		cachedPrintWriter.write(s, off, len);
	}
	
	@Override
	public void flush() {
		super.flush();
		printWriter.flush();
		cachedPrintWriter.flush();
	}
	
	@Override
	public void close() {
		super.close();
		printWriter.close();
		cachedPrintWriter.close();
	}
}
