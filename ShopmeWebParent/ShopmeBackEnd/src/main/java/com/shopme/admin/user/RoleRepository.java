package com.shopme.admin.user;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

@Repository
public interface RoleRepository extends CrudRepository<Role, Integer> {
 
}
