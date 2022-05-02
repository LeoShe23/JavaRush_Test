package com.game.service;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exceptions.BadRequestException;
import com.game.exceptions.NotFoundException;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {
    private PlayerRepository repository;

    @Autowired
    public void setRepository(PlayerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Player createPlayer(Player player) {
        if (!isValidPlayer(player))
            throw new BadRequestException("Some fields are not filled in or filled in incorrectly");
        player.setBanned(player.getBanned() == null ? false : player.getBanned());
        player.setLevel(updateLevel(player.getExperience()));
        player.setUntilNextLevel(updateUntilLevel(player.getExperience(), player.getLevel()));
        return repository.save(player);
    }

    @Override
    public Page<Player> findAllPlayers(Specification<Player> specification, Pageable pageable) {
        return repository.findAll(specification, pageable);
    }

    @Override
    public List<Player> findAllPlayers(Specification<Player> specification) {
        return repository.findAll(specification);
    }

    @Override
    public Player updatePlayer(Long id, Player player) {
        Player updatedPlayer = findPlayerByID(id);

        if (player.getName() != null) updatedPlayer.setName(player.getName());
        if (player.getTitle() != null) updatedPlayer.setTitle(player.getTitle());
        if (player.getRace() != null) updatedPlayer.setRace(player.getRace());
        if (player.getProfession() != null) updatedPlayer.setProfession(player.getProfession());
        if (player.getBanned() != null) updatedPlayer.setBanned(player.getBanned());
        if (player.getBirthday() != null) {
            if (!isValidDate(player.getBirthday()))
                throw new BadRequestException("Wrong date of birth");
            updatedPlayer.setBirthday(player.getBirthday());
        }
        if (player.getExperience() != null) {
            if (!isValidExperience(player.getExperience()))
                throw new BadRequestException("Experience exceeds acceptable values");
            updatedPlayer.setExperience(player.getExperience());
            updatedPlayer.setLevel(updateLevel(updatedPlayer.getExperience()));
            updatedPlayer.setUntilNextLevel(updateUntilLevel(updatedPlayer.getExperience(), updatedPlayer.getLevel()));
        }

        return repository.save(updatedPlayer);
    }

    @Override
    public void deletePlayer(Long id) {
        findPlayerByID(id);
        repository.deleteById(id);
    }

    @Override
    public Player findPlayerByID(Long id) {
        if (id > Long.MAX_VALUE || id <= 0) throw new BadRequestException("Wrong id");
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Player not found"));
    }

    @Override
    public Specification<Player> filterByName(String name) {
        return (root, query, builder) -> name == null ? null : builder.like(root.get("name"), "%" + name + "%");
    }

    @Override
    public Specification<Player> filterByTitle(String title) {
        return (root, query, builder) -> title == null ? null : builder.like(root.get("title"), "%" + title + "%");
    }

    @Override
    public Specification<Player> filterByRace(Race race) {
        return (root, query, builder) -> race == null ? null : builder.equal(root.get("race"), race);
    }

    @Override
    public Specification<Player> filterByProfession(Profession profession) {
        return (root, query, builder) -> profession == null ? null : builder.equal(root.get("profession"), profession);
    }

    @Override
    public Specification<Player> filterByDate(Long after, Long before) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (after == null && before == null)
                return null;
            else if (after == null)
                return criteriaBuilder.lessThanOrEqualTo(root.get("birthday"), new Date(before));
            else if (before == null)
                return criteriaBuilder.greaterThanOrEqualTo(root.get("birthday"), new Date(after));
            else
                return criteriaBuilder.between(root.get("birthday"), new Date(after), new Date(before));
        };
    }

    @Override
    public Specification<Player> filterByBanned(Boolean banned) {
        return (root, query, builder) -> banned == null ? null : banned ? builder.isTrue(root.get("banned")) : builder.isFalse(root.get("banned"));
    }

    @Override
    public Specification<Player> filterByExperience(Integer minExperience, Integer maxExperience) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (minExperience == null && maxExperience == null)
                return null;
            else if (minExperience == null)
                return criteriaBuilder.lessThanOrEqualTo(root.get("experience"), maxExperience);
            else if (maxExperience == null)
                return criteriaBuilder.greaterThanOrEqualTo(root.get("experience"), minExperience);
            else
                return criteriaBuilder.between(root.get("experience"), minExperience, maxExperience);
        };
    }

    @Override
    public Specification<Player> filterByLevel(Integer minLevel, Integer maxLevel) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (minLevel == null && maxLevel == null)
                return null;
            else if (minLevel == null)
                return criteriaBuilder.lessThanOrEqualTo(root.get("level"), maxLevel);
            else if (maxLevel == null)
                return criteriaBuilder.greaterThanOrEqualTo(root.get("level"), minLevel);
            else
                return criteriaBuilder.between(root.get("level"), minLevel, maxLevel);
        };
    }

    private int updateLevel(int experience) {
        return  (int) ((Math.sqrt(2500 + 200 * experience) - 50) / 100);
    }
    private int updateUntilLevel(int experience, int level) {
        return 50 * (level + 1) * (level + 2) - experience;
    }

    private boolean isValidPlayer(Player player) {
        return player != null && player.getRace() != null && player.getProfession() != null &&
                isValidName(player.getName()) && isValidTitle(player.getTitle()) &&
                isValidExperience(player.getExperience()) && isValidDate(player.getBirthday());
    }

    private boolean isValidName(String name) {
        return name != null && name.length() <= 12 && !name.isEmpty();
    }

    private boolean isValidTitle(String title) {
        return title != null && title.length() <= 30;
    }

    private boolean isValidExperience(Integer experience) {
        return experience != null && experience > 0 && experience <= 10_000_000;
    }

    private boolean isValidDate(Date date) {

        return date != null && date.getTime() >= 0 &&
                date.getTime() >= 946674000482L && date.getTime() <= 32535205199494L;
    }
}
