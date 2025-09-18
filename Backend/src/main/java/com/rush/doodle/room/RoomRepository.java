package com.rush.doodle.room;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room,Long> {
	public Optional<Room> findByRoomId(String roomId);
	public List<Room> findByLastActiveBefore(LocalDateTime roomCutoff);
}
