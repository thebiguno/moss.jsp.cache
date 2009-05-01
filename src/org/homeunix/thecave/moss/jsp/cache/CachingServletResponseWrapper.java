package org.homeunix.thecave.moss.jsp.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CachingServletResponseWrapper extends HttpServletResponseWrapper {
		private final HttpServletResponse response;
		
		private CachingPrintWriter printWriter = null;
		private CachingServletOutputStream outputStream = null;
		
		private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		public CachingServletResponseWrapper(HttpServletResponse response){
			super(response);
			this.response = response;
		}
		
		@Override
		public PrintWriter getWriter() throws IOException {
			if (printWriter != null)
				throw new IllegalStateException("Invalid state");
			printWriter = new CachingPrintWriter(response.getWriter());
			return printWriter;
		}
	
		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			if (outputStream != null)
				throw new IllegalStateException("Invalid state");
			outputStream = new CachingServletOutputStream(baos);
			return outputStream;
		}
		
		
		
		public byte[] getData(){
			if (outputStream != null){
				byte[] data = baos.toByteArray();
				try {
					response.getOutputStream().write(data);
				}
				catch (IOException ioe){
					ioe.printStackTrace();
				}
				if (data.length > 0)
					return data;
			}
			else if (printWriter != null){
				return printWriter.getData();
			}
			return null;
		}
//		
//		
//		public void addCookie(Cookie arg0) {
//			response.addCookie(arg0);
//		}
//	
//		public void addDateHeader(String arg0, long arg1) {
//			response.addDateHeader(arg0, arg1);
//		}
//	
//		public void addHeader(String arg0, String arg1) {
//			response.addHeader(arg0, arg1);
//		}
//	
//		public void addIntHeader(String arg0, int arg1) {
//			response.addIntHeader(arg0, arg1);
//		}
//	
//		public boolean containsHeader(String arg0) {
//			return response.containsHeader(arg0);
//		}
//	
//		public String encodeRedirectURL(String arg0) {
//			return response.encodeRedirectURL(arg0);
//		}
//	
//		public String encodeURL(String arg0) {
//			return response.encodeURL(arg0);
//		}
//	
//		public void flushBuffer() throws IOException {
//			response.flushBuffer();
//		}
//	
//		public int getBufferSize() {
//			return response.getBufferSize();
//		}
//	
//		public String getCharacterEncoding() {
//			return response.getCharacterEncoding();
//		}
//	
//		public String getContentType() {
//			return response.getContentType();
//		}
//	
//		public Locale getLocale() {
//			return response.getLocale();
//		}
//	
//		public boolean isCommitted() {
//			return response.isCommitted();
//		}
//	
//		public void reset() {
//			response.reset();
//		}
//	
//		public void resetBuffer() {
//			response.resetBuffer();
//		}
//	
//		public void sendError(int arg0, String arg1) throws IOException {
//			response.sendError(arg0, arg1);
//		}
//	
//		public void sendError(int arg0) throws IOException {
//			response.sendError(arg0);
//		}
//	
//		public void sendRedirect(String arg0) throws IOException {
//			response.sendRedirect(arg0);
//		}
//	
//		public void setBufferSize(int arg0) {
//			response.setBufferSize(arg0);
//		}
//	
//		public void setCharacterEncoding(String arg0) {
//			response.setCharacterEncoding(arg0);
//		}
//	
//		public void setContentLength(int arg0) {
//			response.setContentLength(arg0);
//		}
//	
//		public void setContentType(String arg0) {
//			response.setContentType(arg0);
//		}
//	
//		public void setDateHeader(String arg0, long arg1) {
//			response.setDateHeader(arg0, arg1);
//		}
//	
//		public void setHeader(String arg0, String arg1) {
//			response.setHeader(arg0, arg1);
//		}
//	
//		public void setIntHeader(String arg0, int arg1) {
//			response.setIntHeader(arg0, arg1);
//		}
//	
//		public void setLocale(Locale arg0) {
//			response.setLocale(arg0);
//		}
//	
//		public void setStatus(int arg0) {
//			response.setStatus(arg0);
//		}
//		
//		public String encodeRedirectUrl(String arg0) {
//			throw new RuntimeException("Method encodeRedirectUrl is deprecated, and not implemented in this filter.");
//		}
//		
//		public String encodeUrl(String arg0) {
//			throw new RuntimeException("Method encodeUrl is deprecated, and not implemented in this filter.");
//		}
//		
//		public void setStatus(int arg0, String arg1) {
//			throw new RuntimeException("Method setStatus is deprecated, and not implemented in this filter.");
//		}
}
