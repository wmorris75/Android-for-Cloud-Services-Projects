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
package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;

import java.util.*;

@Controller
public class MainController {

	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
	
	private VideoFileManager videoDataMgr;	
	private static final AtomicLong currentId = new AtomicLong(0L);		
	private Map<Long,Video> videos = new HashMap<Long, Video>();
	
	//Return a collection of videos
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList(){		
		return videos.values();		
	}
	
	//Save and add videos to the server
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v){		
		String dataUrl = getDataUrl(v.getId());
		v.setDataUrl(dataUrl);
		return save(v);
	}
	
	//
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable("id") long id, 
			final @RequestParam("data") MultipartFile videoData, HttpServletResponse response){
			
			try {
				
				if(!videos.containsKey(id)){
					response.setStatus(404);
					return new VideoStatus(VideoState.READY);
				}
				//saveSomeVideo(videos.get(id), (MultipartFile) videoData.getInputStream());
				 videoDataMgr = VideoFileManager.get();
		 	     videoDataMgr.saveVideoData(videos.get(id), videoData.getInputStream());
			} catch (IOException e){
				e.printStackTrace();
			}
			return new VideoStatus(VideoState.READY);
	}
	
	//Get 
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.GET)
	public @ResponseBody void getData(@PathVariable ("id") long id, HttpServletResponse response){
		
		try {
			if(!videos.containsKey(id)){
				response.setStatus(404);
			}
			else{
			serveSomeVideo(videos.get(id), response);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	//Send Video files to client
 	public void serveSomeVideo(Video v, HttpServletResponse response) throws IOException {
 	     // Of course, you would need to send some headers, etc. to the
 	     // client too!
 	     //  ...
 		 response.setContentType("application/json");
 	     videoDataMgr.copyVideoData(v, response.getOutputStream());
 	}	
	
		//Get video 
	  	public Video save(Video entity) {
			checkAndSetId(entity);
			videos.put(entity.getId(), entity);
			return entity;
		}

		private void checkAndSetId(Video entity) {
			if(entity.getId() == 0){
				entity.setId(currentId.incrementAndGet());
			}
		}
		
		//Extract Url
		private String getDataUrl(long videoId){
            String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
            return url;
        }

     	private String getUrlBaseForLocalServer() {
		   HttpServletRequest request = 
		       ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		   String base = 
		      "http://"+request.getServerName() 
		      + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
		   return base;
		}
	
}

