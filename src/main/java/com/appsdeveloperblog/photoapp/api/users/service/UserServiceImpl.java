package com.appsdeveloperblog.photoapp.api.users.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.appsdeveloperblog.photoapp.api.users.data.AlbumServiceClient;
import com.appsdeveloperblog.photoapp.api.users.data.UserEntity;
import com.appsdeveloperblog.photoapp.api.users.data.UserRepository;
import com.appsdeveloperblog.photoapp.api.users.shared.UserDto;
import com.appsdeveloperblog.photoapp.api.users.ui.model.AlbumResponseModel;

import feign.FeignException;

@Service
public class UserServiceImpl implements UsersService {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	UserRepository userRepository;
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder; 
	
	@Autowired
	AlbumServiceClient albumServiceClient;
	//RestTemplate restTemplate;
	@Autowired
	Environment environment;
	
	@Override
	public UserDto createUser(UserDto userdetails) {
		userdetails.setUserId(UUID.randomUUID().toString());
		userdetails.setEncryptedPassword(bCryptPasswordEncoder.encode(userdetails.getPassword()));
		ModelMapper mapper =new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		UserEntity userEntity= mapper.map(userdetails, UserEntity.class);
		userRepository.save(userEntity);
		UserDto returnValue = mapper.map(userEntity, UserDto.class);
		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity userEntity= userRepository.findByEmail(username);
		if(userEntity == null) throw new UsernameNotFoundException(username);
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true,true,true,true,new ArrayList<>());
		}

	@Override
	public UserDto getUserDetailsByEmail(String email) {
		UserEntity userEntity= userRepository.findByEmail(email);
		if(userEntity == null) throw new UsernameNotFoundException(email);
		return new ModelMapper().map(userEntity,UserDto.class);
	}

	
	@Override
	public UserDto getUserByUserId(String userId) {
		UserEntity userEntity= userRepository.findByUserId(userId);
		if(userEntity == null) throw new UsernameNotFoundException("User not found");
		UserDto userDto = new ModelMapper().map(userEntity,UserDto.class);
		
		/*
		 * String albumsurl = String.format(environment.getProperty("albums.url"),
		 * userId); ResponseEntity<List<AlbumResponseModel>> albumListResponse =
		 * restTemplate.exchange(albumsurl, HttpMethod.GET, null, new
		 * ParameterizedTypeReference<List<AlbumResponseModel>>(){});
		 * 
		 * List<AlbumResponseModel> albumsList = albumListResponse.getBody();
		 */
		
		List<AlbumResponseModel> albumsList = new ArrayList<>();
		
		try {
			logger.debug("Before call Albums Microservice");
			albumsList = albumServiceClient.getAlbums(userId);
			logger.debug("After call Albums Microservice");
		} catch (FeignException e) {
			// TODO Auto-generated catch block
			logger.error(e.getLocalizedMessage());
		}
		
		userDto.setAlbums(albumsList);
		return userDto;
	}
}
