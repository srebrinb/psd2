package com.ibm.api.cashew.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ibm.api.cashew.beans.Tag;
import com.ibm.api.cashew.beans.User;
import com.ibm.api.cashew.db.mongo.MongoTagRepository;
import com.ibm.api.cashew.db.mongo.MongoUserRepository;

@Service
public class UserServiceImpl implements UserService {
	
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private final Logger logger = LogManager.getLogger(UserServiceImpl.class);

	@Autowired
	MongoUserRepository userRepo;

	@Value("${user.role.lowest}")
	private String ROLE_USER;
	
	@Autowired
	private MongoTagRepository tagRepo;

	private User getUserById(String userId) {
		logger.debug("finding user with userId: " + userId);
		return userRepo.findOne(userId);
	}

	@Override
	public User findUserById(String userId) {
		User user = getUserById(userId);
		return user;
	}

	@Override
	public User createUser(User user) {
		logger.debug("Creating user = " + user);

		if (user == null) {
			throw new IllegalArgumentException("User can't be null");
		}

		if (user.getUserId() == null) {
			throw new IllegalArgumentException("userId can't be null");
		}
		/*
		 * if (user.getAuthProvider() == null || user.getAuthProviderClientId()
		 * == null) { throw new IllegalArgumentException(
		 * "Client Details are not set"); }
		 */
		if (getUserById(user.getUserId()) != null) {
			throw new IllegalArgumentException("User already exists");
		}

		user.setLocked(false);

		user = userRepo.save(user);
		return user;
	}

	@Override
	public User updateUser(User user) {
		logger.debug("Updating user = " + user);

		if (user == null) {
			throw new IllegalArgumentException("User can't be null");
		}

		User existingUser = getUserById(user.getUserId());

		if (existingUser == null) {
			throw new IllegalArgumentException("User doesn't exist");
		}

		if (user.getName() != null && !user.getName().isEmpty()) {
			existingUser.setName(user.getName());
		}

		if (user.getMobileNumber() != null && !user.getMobileNumber().isEmpty()) {
			existingUser.setMobileNumber(user.getMobileNumber());
		}

		if (user.getEmail() != null && !user.getEmail().isEmpty()) {
			existingUser.setEmail(user.getEmail());
		}

		if (user.getDateOfBirth() != null) {
			existingUser.setDateOfBirth(user.getDateOfBirth());
		}

		logger.debug("User will be updated as: " + existingUser);

		return userRepo.save(existingUser);
	}

	@Override
	public long changePhone(String userId, String phoneNumber) {
		logger.info("changing user's phonenumber: " + userId + " : " + phoneNumber);
		if (phoneNumber == null || phoneNumber.isEmpty()) {
			throw new IllegalArgumentException("PhoneNumber can't be null");
		}
		return userRepo.updatePhone(userId, phoneNumber);
	}

	@Override
	public long changeEmail(String userId, String email) {
		logger.info("changing user's email: " + userId);
		if (email == null || email.isEmpty()) {
			throw new IllegalArgumentException("PhoneNumber can't be null");
		}
		return userRepo.updateEmail(userId, email);
	}

	@Override
	public long changeDOB(String userId, String dob) {
		logger.info("changing user's dateOfBirth: " + userId + " : " + dob);
		if (dob == null) {
			throw new IllegalArgumentException("Date Of Birth can't be null");
		}
		try
		{
			DATE_FORMAT.parse(dob);
		}
		catch (ParseException e)
		{
			throw new IllegalArgumentException("Incorrect Date format. Specify date in yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		}

		return userRepo.updateDOB(userId, dob);
	}

	@Override
	public long addTags(Set<Tag> tags, String userId) {

		User existingUser = getUserById(userId);

		if (existingUser == null) {
			throw new IllegalArgumentException("User doesn't exist");
		}
		
		if (!CollectionUtils.isEmpty(tags)) {
			
			Set<Tag> existngTags=existingUser.getTags();
			
			if(existngTags!=null){
				
				existngTags.addAll(tags);
			}
			return userRepo.addTag(existngTags, userId);
		}
		return 0;

	}

	@Override
	public Set<Tag> getTags() {
		
		Set<Tag> tags =new HashSet<Tag>(tagRepo.findAll());		
		return tags;
	}
}
