package com.appsdeveloperblog.photoapp.api.users.data;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.appsdeveloperblog.photoapp.api.users.ui.model.AlbumResponseModel;

import feign.FeignException;
import feign.hystrix.FallbackFactory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@FeignClient(name="albums-ws")
public interface AlbumServiceClient{

	@GetMapping("/users/{id}/albums")
	@CircuitBreaker(name="albums-ws", fallbackMethod="getAlbumFallBack")
	public List<AlbumResponseModel> getAlbums(@PathVariable String id);
	
	default List<AlbumResponseModel> getAlbumFallBack(String id, Throwable exception) {
		System.out.println("param = " +id);
		System.out.println("Exception took place"+ exception.getMessage());
		return new ArrayList<>();
		
	}
}
