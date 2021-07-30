package com.example.demo.repository;

import com.example.demo.entity.PlayersList;
import org.springframework.data.repository.CrudRepository;

public interface PlayersListRepo extends CrudRepository<PlayersList,Long> {


}
