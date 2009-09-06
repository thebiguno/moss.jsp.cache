package ca.digitalcave.moss.jsp.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import ca.digitalcave.moss.jsp.cache.io.CachingPrintWriter;
import ca.digitalcave.moss.jsp.cache.io.CachingServletOutputStream;

public class SplitStreamServletResponseWrapper extends HttpServletResponseWrapper {
	private final HttpServletResponse response;

	private CachingPrintWriter printWriter = null;
	private CachingServletOutputStream outputStream = null;

	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	private Map<String, String> headers = new LinkedHashMap<String, String>();
//	private String contentType = "";
	private int status = HttpServletResponse.SC_OK; //Default to 200, if anything else it will be set below.

	public SplitStreamServletResponseWrapper(HttpServletResponse response){
		super(response);
		this.response = response;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		printWriter = new CachingPrintWriter(response.getWriter(), new PrintWriter(baos));
		return printWriter;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		outputStream = new CachingServletOutputStream(response.getOutputStream(), baos);
		return outputStream;
	}

	@Override
	public void setHeader(String name, String value) {
		super.setHeader(name, value);
		this.headers.put(name, value);
	}
	
	@Override
	public void addHeader(String name, String value) {
		super.addHeader(name, value);
		this.headers.put(name, value);
	}
	
	public Map<String, String> getHeaders() {
		return Collections.unmodifiableMap(headers);
	}
	
	@Override
	public void setStatus(int sc) {
		super.setStatus(sc);
		this.status = sc;
	}
	
	@Override
	public void setStatus(int sc, String sm) {
		super.setStatus(sc, sm);
		this.status = sc;
	}
	
	public int getStatus() {
		return status;
	}
	
//	@Override
//	public void setContentType(String type) {
//		super.setContentType(type);
//		this.contentType = type;
//	}
	
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
