package com.medgo.auth.repository.medigo;

import com.medgo.auth.domain.entity.medigo.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserModelRepository extends JpaRepository<UserModel, String> {


}
