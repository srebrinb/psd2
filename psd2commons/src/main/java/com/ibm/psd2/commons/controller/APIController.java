package com.ibm.psd2.commons.controller;


import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;

public abstract class APIController
{
	private static final Logger logger = LogManager.getLogger(APIController.class);

//	protected ResponseEntity<Object> handleException(Throwable e)
//	{
//		ResponseEntity<Object> response;
//		ErrorBean eb = new ErrorBean();
//		logger.error(e.getMessage(), e);
//		eb.setError(e.getMessage());
//		response = ResponseEntity.badRequest().body(eb);
//		return response;
//	}
//
//	protected ResponseEntity<List<Object>> handleExceptions(Throwable e)
//	{
//		ResponseEntity<List<Object>> response;
//		ErrorBean eb = new ErrorBean();
//		logger.error(e.getMessage(), e);
//		eb.setError(e.getMessage());
//		List<Object> rl = Arrays.asList(eb);
//		response = ResponseEntity.badRequest().body(rl);
//		return response;
//	}
//
//	protected ResponseEntity<Object> handleException(Throwable e, boolean badRequest)
//	{
//
//		ResponseEntity<Object> response;
//		ErrorBean eb = new ErrorBean();
//		logger.error(e.getMessage(), e);
//		eb.setError(e.getMessage());
//		response = badRequest ? ResponseEntity.badRequest().body(eb)
//				: ResponseEntity.ok().body(eb);
//		return response;
//	}

}
