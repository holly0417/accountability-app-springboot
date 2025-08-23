package com.github.holly.accountability.tasks;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query(value = """
        SELECT SUM(TIMESTAMPDIFF('SECOND', t.time_start, t.time_end)) as total
        FROM tasks t
        WHERE t.status = :#{#status.name()}
        AND t.user_id = :userId
        """, nativeQuery = true)
    Double getTotalSecondsWithEndTime(Long userId,
                                      TaskStatus status);

    @Query(value = """
        SELECT SUM(TIMESTAMPDIFF('SECOND', t.time_start, CURRENT_TIMESTAMP)) as total
        FROM tasks t
        WHERE t.status = :#{#status.name()}
        AND t.user_id = :userId
        """, nativeQuery = true)
    Double getTotalSecondsNoEndTime(Long userId,
                                    TaskStatus status);

    @Query("""
        FROM Task t
        WHERE t.status in (:statuses)
        AND t.user.id = :userId
        """)
    Page<Task> findByUserId(Long userId, List<TaskStatus> statuses, Pageable pageable);

    @Query("""
        FROM Task t 
        WHERE t.user.id in (:userIds)
        AND t.status in (:statuses)
        """)
    Page<Task> findByUserIdIn(List<Long> userIds, List<TaskStatus> statuses, Pageable pageable);

}

