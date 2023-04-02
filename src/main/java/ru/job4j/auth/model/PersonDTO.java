package ru.job4j.auth.model;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author: Egor Bekhterev
 * @date: 02.04.2023
 * @project: job4j_auth
 */
@Data
public class PersonDTO {

    @Length(min = 6, max = 20, message = "Password length must be between {min} and {max} characters.")
    private String password;
}
