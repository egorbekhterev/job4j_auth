package ru.job4j.auth.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import ru.job4j.auth.validation.Operation;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author: Egor Bekhterev
 * @date: 31.03.2023
 * @project: job4j_auth
 */
@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @NotNull(message = "Id must be not null.", groups = {Operation.OnUpdate.class})
    private int id;

    @NotBlank(message = "Login must be not empty.", groups = {Operation.OnUpdate.class, Operation.OnCreate.class})
    private String login;

    @Length(min = 6, max = 20, message = "Password length must be between {min} and {max} characters.",
            groups = {Operation.OnUpdate.class, Operation.OnCreate.class})
    private String password;
}
