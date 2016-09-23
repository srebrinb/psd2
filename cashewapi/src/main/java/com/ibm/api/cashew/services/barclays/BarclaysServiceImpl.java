package com.ibm.api.cashew.services.barclays;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.ibm.api.cashew.beans.UserAccount;
import com.ibm.api.cashew.beans.barclays.Account;
import com.ibm.api.cashew.beans.barclays.Customer;
import com.ibm.api.cashew.beans.barclays.Transaction;
import com.ibm.api.cashew.utils.UUIDGenerator;
import com.ibm.psd2.datamodel.Amount;
import com.ibm.psd2.datamodel.Challenge;
import com.ibm.psd2.datamodel.aip.BankAccountDetailsView;
import com.ibm.psd2.datamodel.aip.TransactionAccount;
import com.ibm.psd2.datamodel.aip.TransactionBank;
import com.ibm.psd2.datamodel.aip.TransactionDetails;
import com.ibm.psd2.datamodel.subscription.SubscriptionInfo;
import com.ibm.psd2.datamodel.subscription.SubscriptionRequest;

@Service
public class BarclaysServiceImpl implements BarclaysService {

	@Autowired
	RestTemplate restTemplate;

	@Value("${barclays.account.service.url}")
	private String acctServiceUrl;

	@Value("${barclays.customer.service.url}")
	private String custServiceUrl;

	@Value("${barclays.id}")
	private String barclaysBank;

	public List<com.ibm.psd2.datamodel.aip.Transaction> getTransactions(String accountId) throws URISyntaxException {

		String url = acctServiceUrl + "/accounts/" + accountId + "/transactions";
		URI uri = new URI(url);

		RequestEntity<Void> rea = RequestEntity.get(uri).accept(MediaType.APPLICATION_JSON).build();
		Map<String, String> uriVariables = new HashMap<String, String>();
		uriVariables.put("accountId", accountId);

		ResponseEntity<List<Transaction>> txn = restTemplate.exchange(rea,
				new ParameterizedTypeReference<List<Transaction>>() {
				});

		List<Transaction> txnList = txn.getBody();

		List<com.ibm.psd2.datamodel.aip.Transaction> txnRes = null;

		if (!CollectionUtils.isEmpty(txn.getBody())) {

			txnRes = new ArrayList<com.ibm.psd2.datamodel.aip.Transaction>();
			for (Transaction transaction : txnList) {

				txnRes.add(populateTransactionRespose(transaction, accountId));
			}

		}

		return txnRes;
	}

	private com.ibm.psd2.datamodel.aip.Transaction populateTransactionRespose(Transaction transaction,
			String accountId) {

		com.ibm.psd2.datamodel.aip.Transaction txn = new com.ibm.psd2.datamodel.aip.Transaction();

		txn.setId(transaction.getId());
		TransactionDetails txnDetails = new TransactionDetails();
		Amount amt = new Amount();

		if (transaction.getAmount() != null) {

			double moneyIn = Double.valueOf(transaction.getAmount().getMoneyIn());
			double moneyOut = Double.valueOf(transaction.getAmount().getMoneyOut());

			if (moneyIn > 0.00) {

				amt.setAmount(moneyIn);
			} else {

				amt.setAmount(moneyOut);
			}

		}

		txnDetails.setValue(amt);
		Amount accBalance = new Amount();

		if (transaction.getAccountBalanceAfterTransaction() != null
				&& StringUtils.isNotBlank(transaction.getAccountBalanceAfterTransaction().getAmount())) {

			accBalance.setAmount(Double.valueOf(transaction.getAccountBalanceAfterTransaction().getAmount()));

		}

		if (StringUtils.isNotBlank(transaction.getCreated())) {

			txnDetails.setCompleted(com.ibm.psd2.datamodel.aip.Transaction.DATE_FORMAT.format(new Date()));
			txnDetails.setPosted(com.ibm.psd2.datamodel.aip.Transaction.DATE_FORMAT.format(new Date()));

		}

		txnDetails.setDescription(transaction.getDescription());
		txnDetails.setNewBalance(accBalance);
		txn.setDetails(txnDetails);

		TransactionBank fromTxnBank = new TransactionBank();
		fromTxnBank.setName(barclaysBank);
		fromTxnBank.setNationalIdentifier(barclaysBank);

		TransactionAccount thisAcct = new TransactionAccount();
		thisAcct.setId(accountId);
		thisAcct.setBank(fromTxnBank);

		txn.setThisAccount(thisAcct);

		TransactionAccount otherAcct = new TransactionAccount();
		otherAcct.setId(transaction.getPaymentDescriptor().getId());

		TransactionBank toTxnBank = new TransactionBank();
		fromTxnBank.setName(transaction.getPaymentDescriptor().getName());
		fromTxnBank.setNationalIdentifier(barclaysBank);
		otherAcct.setBank(toTxnBank);

		txn.setOtherAccount(otherAcct);

		return txn;
	}

	@Override
	public SubscriptionRequest subscribe(SubscriptionRequest subscriptionRequest) {

		Challenge challenge = new Challenge();
		challenge.setId(subscriptionRequest.getSubscriptionInfo().getBankId() + "-" + UUIDGenerator.generateUUID());
		challenge.setChallengeType(Challenge.ACCOUNT_SUBSCRIPTION);
		challenge.setAnswer(UUIDGenerator.generateUUID());
		challenge.setAllowedAttempts(Challenge.CHALLENGE_MAX_ATTEMPTS);

		subscriptionRequest.setChallenge(challenge);
		subscriptionRequest.setStatus(SubscriptionRequest.STATUS_SUBSCRIBED);

		subscriptionRequest.getSubscriptionInfo().setStatus(SubscriptionInfo.STATUS_ACTIVE);

		return subscriptionRequest;
	}

	@Override
	public Map<String, BankAccountDetailsView> getAccountInformation(UserAccount ua) throws URISyntaxException {

		Customer customer = getUserAccounts(ua.getAccount().getUsername());
		Map<String, BankAccountDetailsView> accts = populateCustomerAcct(customer);
		return accts;
	}

	private Map<String, BankAccountDetailsView> populateCustomerAcct(Customer customer) {

		if (customer != null && !CollectionUtils.isEmpty(customer.getAccountList())) {

			List<Account> acctList = customer.getAccountList();

			if (!CollectionUtils.isEmpty(acctList)) {

				Map<String, BankAccountDetailsView> bankAccts = new HashMap<String, BankAccountDetailsView>();

				for (Account acct : acctList) {
					BankAccountDetailsView bankAcctDetails = new BankAccountDetailsView();
					bankAcctDetails.setBankId(barclaysBank);
					bankAcctDetails.setId(acct.getId());

					Amount amt = new Amount();

					if (!StringUtils.isEmpty(acct.getCurrentBalance())) {
						amt.setAmount(Double.valueOf(acct.getCurrentBalance()));
					}

					bankAcctDetails.setBalance(amt);
					bankAcctDetails.setUsername(customer.getId());
					bankAcctDetails.setType(acct.getAccountType());
					bankAccts.put(acct.getId(), bankAcctDetails);
				}

				return bankAccts;
			}
		}

		return null;
	}

	private Customer getUserAccounts(String customerId) throws URISyntaxException {

		String url = custServiceUrl + "/customers/{customerId}";

		Map<String, String> uriVariables = new HashMap<String, String>();
		uriVariables.put("customerId", customerId);
		Customer customer = restTemplate.getForObject(url, Customer.class, uriVariables);

		return customer;
	}

}