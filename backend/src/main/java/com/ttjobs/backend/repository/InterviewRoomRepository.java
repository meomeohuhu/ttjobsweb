package com.ttjobs.backend.repository;

import com.ttjobs.backend.entity.InterviewRoom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterviewRoomRepository extends JpaRepository<InterviewRoom, Long> {
    Optional<InterviewRoom> findByInterviewId(Long interviewId);
    Optional<InterviewRoom> findByRoomId(String roomId);

    @Query("""
            select room
            from InterviewRoom room
            left join fetch room.interview interview
            left join fetch interview.recruiter
            left join fetch interview.candidate
            where room.roomId = :roomId
            """)
    Optional<InterviewRoom> findByRoomIdWithParticipants(@Param("roomId") String roomId);
}
