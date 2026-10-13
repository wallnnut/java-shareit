package ru.practicum.shareit.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Test
    void createUpdateGetDeleteAndDuplicateEmail() {
        UserDto created = userService.create(UserDto.builder().name("Ann").email("ann-svc@mail.com").build());
        UserDto other = userService.create(UserDto.builder().name("Bob").email("bob-svc@mail.com").build());

        assertThat(created.getId()).isNotNull();
        assertThat(userService.getById(created.getId()).getEmail()).isEqualTo("ann-svc@mail.com");
        assertThat(userService.getAll()).extracting(UserDto::getId).contains(created.getId(), other.getId());

        UserDto renamed = userService.update(created.getId(), UserDto.builder().name("Anna").build());
        assertThat(renamed.getName()).isEqualTo("Anna");
        assertThat(renamed.getEmail()).isEqualTo("ann-svc@mail.com");

        UserDto sameEmail = userService.update(created.getId(),
                UserDto.builder().email("ann-svc@mail.com").name("  ").build());
        assertThat(sameEmail.getName()).isEqualTo("Anna");
        assertThat(sameEmail.getEmail()).isEqualTo("ann-svc@mail.com");

        UserDto newEmail = userService.update(created.getId(), UserDto.builder().email("anna-svc@mail.com").build());
        assertThat(newEmail.getEmail()).isEqualTo("anna-svc@mail.com");

        UserDto duplicate = UserDto.builder().name("Clone").email("bob-svc@mail.com").build();
        assertThatThrownBy(() -> userService.create(duplicate)).isInstanceOf(ConflictException.class);
        UserDto taken = UserDto.builder().email("bob-svc@mail.com").build();
        assertThatThrownBy(() -> userService.update(created.getId(), taken)).isInstanceOf(ConflictException.class);

        userService.deleteById(created.getId());
        assertThatThrownBy(() -> userService.getById(created.getId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> userService.deleteById(created.getId())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> userService.update(created.getId(), UserDto.builder().name("X").build()))
                .isInstanceOf(NotFoundException.class);
    }
}
