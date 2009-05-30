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
}
