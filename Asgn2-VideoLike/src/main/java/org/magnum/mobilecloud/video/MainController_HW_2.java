/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;

import retrofit.http.GET;
import retrofit.http.Path;

@Controller
public class MainController_HW_2 {

	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it to
	 * something other than "AnEmptyController"
	 * 
	 * 
	 * ________ ________ ________ ________ ___ ___ ___ ________ ___ __ |\
	 * ____\|\ __ \|\ __ \|\ ___ \ |\ \ |\ \|\ \|\ ____\|\ \|\ \ \ \ \___|\ \
	 * \|\ \ \ \|\ \ \ \_|\ \ \ \ \ \ \ \\\ \ \ \___|\ \ \/ /|_ \ \ \ __\ \ \\\
	 * \ \ \\\ \ \ \ \\ \ \ \ \ \ \ \\\ \ \ \ \ \ ___ \ \ \ \|\ \ \ \\\ \ \ \\\
	 * \ \ \_\\ \ \ \ \____\ \ \\\ \ \ \____\ \ \\ \ \ \ \_______\ \_______\
	 * \_______\ \_______\ \ \_______\ \_______\ \_______\ \__\\ \__\
	 * \|_______|\|_______|\|_______|\|_______|
	 * \|_______|\|_______|\|_______|\|__| \|__|
	 * 
	 * 
	 */

	@Autowired
	private VideoRepository videos;

	// private Video v = null;

	// Return a collection of videos
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return (Collection<Video>) videos.findAll();
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "{/id}", method = RequestMethod.GET)
	public @ResponseBody
	Video getVideo(@PathVariable("id") long id, HttpServletResponse response) {
		Video v = videos.findOne(id);
		if (v == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		return v;
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/id", method = RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id) {
		return (Video) videos.getVideoById(id);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		v.setLikes(0);
		return (Video) videos.save(v);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<Void> likeVideo(
			@PathVariable("id") long id, Principal p) {

		if (videos.findOne(id) == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);			 
		}

		String username = p.getName();
		Video v = videos.findOne(id);
		Set<String> likesUsernames = v.getLikesUsernames();
		
		// Checks if the user has already liked the video.
		if (likesUsernames.contains(username)) {	
			//videos.save(v);
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}	
		
		likesUsernames.add(username);
		v.setLikesUsernames(likesUsernames);
		v.setLikes(likesUsernames.size());
		//v.setLikes(1);
		videos.save(v);		
		return new ResponseEntity<Void>(HttpStatus.OK);	
	}

	@RequestMapping(value = "/video/{id}/unlike", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<Void> unlikeVideo(
			@PathVariable("id") long id, Principal p) {
		
		if (videos.findOne(id) == null) {
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}

		String username = p.getName();
		Video v = videos.findOne(id);
		Set<String> likesUsernames = (Set<String>) v.getLikesUsernames();			
				
		// Checks if the user has already liked the video.
		if (likesUsernames.contains(username)) {							
				likesUsernames.remove(username);
				v.setLikesUsernames(likesUsernames);
				v.setLikes(likesUsernames.size());				
				videos.save(v);	
				
				return new ResponseEntity<Void>(HttpStatus.OK);	
		}				
		//videos.save(v);
		return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
	}  

	@RequestMapping(value = VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(
			@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title) {
		return videos.findByName(title);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(
			@RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration) {
		return videos.findByDurationLessThan(duration);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method = RequestMethod.GET)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(
			@PathVariable("id") long id, Principal p) {
		if (!videos.exists(id)) {
			new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}

		Video v = (Video) videos.findOne(id);
		Set<String> likesUsernames = v.getLikesUsernames();
		return (Collection<String>) likesUsernames;
	}

}
