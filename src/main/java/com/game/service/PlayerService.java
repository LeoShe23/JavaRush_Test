package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PlayerService {
    public Player createPlayer(Player player);

    public Page<Player> findAllPlayers(Specification<Player> specification, Pageable pageable);

    public List<Player> findAllPlayers(Specification<Player> specification);

    public Player updatePlayer(Long id, Player player);

    public void deletePlayer(Long id);

    public Player findPlayerByID(Long id);

    public Specification<Player> filterByName(String name);

    public Specification<Player> filterByTitle(String name);

    public Specification<Player>filterByRace(Race race);

    public Specification<Player> filterByProfession(Profession profession);

    public Specification<Player> filterByDate(Long after, Long before);

    public Specification<Player> filterByBanned(Boolean banned);

    public Specification<Player> filterByExperience(Integer minExperience, Integer maxExperience);

    public Specification<Player> filterByLevel(Integer minLevel, Integer maxLevel);
}
