package ru.practicum.shareit.user;

import java.util.List;
import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {

    UserDto create(UserDto userDto);

    UserDto update(Long userId, UserDto userDto);

    UserDto getById(Long userId);

    List<UserDto> getAll();

    void deleteById(Long userId);
}
