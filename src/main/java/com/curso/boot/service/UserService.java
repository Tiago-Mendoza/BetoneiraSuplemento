package com.curso.boot.service;

import com.curso.boot.dto.RegistrationForm;
import com.curso.boot.exception.DuplicateUserFieldException;
import com.curso.boot.dao.UserDao;
import com.curso.boot.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegistrationForm form) throws DuplicateUserFieldException {
        if (userDao.findByEmail(form.getEmail()) != null) {
            throw new DuplicateUserFieldException("email", "Este e-mail já está em uso.");
        }

        String rawCpf = form.getCpf().replaceAll("\\D", "");
        if (userDao.findByCpf(rawCpf) != null) {
            throw new DuplicateUserFieldException("cpf", "Este CPF já está em uso.");
        }

        User user = new User();
        user.setEmail(form.getEmail());
        user.setCpf(rawCpf);
        user.setNome(form.getNome());
        user.setTelefoneCelular(form.getTelefoneCelular());
        user.setTelefoneFixo(form.getTelefoneFixo());
        user.setGenero(form.getGenero());

        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRoles(Collections.singletonList("ROLE_USER"));

        userDao.save(user);
    }

    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public List<User> findAll() {
        return userDao.findAll();
    }

    @Transactional
    public void createAdminIfNotExists(String email, String password, String name) {
        if (userDao.findByEmail(email) == null) {
            User admin = new User();
            admin.setEmail(email);
            admin.setCpf("00000000000"); // CPF reservado para admin
            admin.setNome(name);
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setRoles(Collections.singletonList("ROLE_ADMIN"));
            userDao.save(admin);
        }
    }
}
