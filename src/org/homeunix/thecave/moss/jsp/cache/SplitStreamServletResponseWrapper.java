package org.homeunix.thecave.moss.jsp.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.homeunix.thecave.moss.jsp.cache.io.CachingPrintWriter;
import org.homeunix.thecave.moss.jsp.cache.io.CachingServletOutputStream;

public class SplitStreamServletResponseWrapper extends HttpServletResponseWrapper {
	private final HttpServletResponse response;

	private CachingPrintWriter printWriter = null;
	private CachingServletOutputStream outputStream = null;

	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

	public SplitStreamServletResponseWrapper(HttpServletResponse response){
		super(response);
		this.response = response;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (printWriter != null || outputStream != null)
			throw new IllegalStateException("Invalid state");
		printWriter = new CachingPrintWriter(response.getWriter(), new PrintWriter(baos));
		return printWriter;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (printWriter != null || outputStream != null)
			throw new IllegalStateException("Invalid state");
		outputStream = new CachingServletOutputStream(response.getOutputStream(), baos);
		return outputStream;
	}

	public byte[] getData() throws IOException {
		if (printWriter != null)
			printWriter.flush();
		if (outputStream != null)
			outputStream.flush();

		byte[] data = baos.toByteArray();
		if (data.length > 0)
			return data;
		return null;
	}	
}
