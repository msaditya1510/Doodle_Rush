package com.rush.doodle.player;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rush.doodle.room.Room;
@Repository
public interface PlayerRepository extends JpaRepository<Player,Long> {
	public List<Player> findByRoom_Id(Long id);

	public Optional<Player> findByName(String name);
	public List<Player> findByLastActiveBefore(LocalDateTime cutoff);
	Optional<Player> findByNameAndRoom_RoomId(String name, String roomId);

	public List<Player> findByRoom(Room room);
}
