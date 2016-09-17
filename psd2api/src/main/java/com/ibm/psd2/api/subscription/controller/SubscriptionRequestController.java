package com.ibm.psd2.api.subscription.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.psd2.api.subscription.service.SubscriptionRequestService;
import com.ibm.psd2.api.subscription.service.SubscriptionService;
import com.ibm.psd2.datamodel.ChallengeAnswer;
import com.ibm.psd2.datamodel.subscription.SubscriptionInfo;
import com.ibm.psd2.datamodel.subscription.SubscriptionRequest;

@RestController
public class SubscriptionRequestController
{

	@Autowired
	SubscriptionRequestService subsReqService;

	@Autowired
	SubscriptionService subsService;

	private final Logger logger = LogManager.getLogger(SubscriptionRequestController.class);

	@Value("${version}")
	private String version;

	@RequestMapping(method = RequestMethod.POST, value = "/subscriptionRequest", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<SubscriptionRequest> createSubscription(
			@RequestBody(required = true) SubscriptionRequest s)
	{
		ResponseEntity<SubscriptionRequest> response;
		try
		{
			SubscriptionRequest sreturn = subsReqService.createSubscriptionRequest(s);
			response = ResponseEntity.ok(sreturn);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			response = ResponseEntity.badRequest().body(null);
		}
		return response;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/subscription/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<List<SubscriptionInfo>> getSubscriptionInfo(
			@PathVariable("username") String username)
	{
		ResponseEntity<List<SubscriptionInfo>> response;
		try
		{
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			List<SubscriptionInfo> sreturn = subsService.getSubscriptionInfo(username, auth.getName());
			response = ResponseEntity.ok(sreturn);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			response = ResponseEntity.badRequest().body(null);
		}
		return response;
	}

	@RequestMapping(method = RequestMethod.PATCH, value = "/subscription/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<SubscriptionInfo> activateSubscription(@PathVariable("id") String id,
			@RequestBody(required = true) ChallengeAnswer cab)
	{
		ResponseEntity<SubscriptionInfo> response;
		try
		{
			SubscriptionInfo si = subsReqService.validateTxnChallengeAnswer(id, cab);
			response = ResponseEntity.ok(si);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			response = ResponseEntity.badRequest().body(null);
		}
		return response;
	}
}
