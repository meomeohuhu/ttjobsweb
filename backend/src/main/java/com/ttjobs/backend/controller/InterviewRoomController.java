package com.ttjobs.backend.controller;

import com.ttjobs.backend.dto.interview.InterviewRoomDTO;
import com.ttjobs.backend.dto.interview.InterviewSignalRequest;
import com.ttjobs.backend.service.InterviewRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interviews")
public class InterviewRoomController {

    @Autowired
    private InterviewRoomService interviewRoomService;

    @PostMapping("/{interviewId}/rooms")
    public InterviewRoomDTO createRoom(@PathVariable Long interviewId) {
        return interviewRoomService.createRoom(interviewId);
    }

    @PostMapping("/{interviewId}/rooms/join")
    public InterviewRoomDTO join(@PathVariable Long interviewId) {
        return interviewRoomService.join(interviewId);
    }

    @PostMapping("/{interviewId}/rooms/leave")
    public InterviewRoomDTO leave(@PathVariable Long interviewId) {
        return interviewRoomService.leave(interviewId);
    }

    @PostMapping("/{interviewId}/rooms/end")
    public InterviewRoomDTO end(@PathVariable Long interviewId) {
        return interviewRoomService.end(interviewId);
    }

    @PostMapping("/{interviewId}/rooms/signal")
    public void signal(@PathVariable Long interviewId, @RequestBody(required = false) InterviewSignalRequest request) {
        interviewRoomService.signal(interviewId, request);
    }
}
