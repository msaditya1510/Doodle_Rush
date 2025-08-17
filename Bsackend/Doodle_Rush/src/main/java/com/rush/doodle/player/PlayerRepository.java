package com.rush.doodle.player;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface PlayerRepository extends JpaRepository<Player,Long> {
	public List<Player> findByRoom_Id(Long id);

	public Optional<Player> findByName(String name);
}
