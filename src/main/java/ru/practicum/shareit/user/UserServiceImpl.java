package ru.practicum.shareit.user;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto create(UserDto userDto) {
        assertEmailIsFree(userDto.getEmail(), null);
        User user = UserMapper.toUser(userDto);
        user.setId(null);
        return UserMapper.toUserDto(userStorage.save(user));
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User user = getUserOrThrow(userId);
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            assertEmailIsFree(userDto.getEmail(), userId);
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        return UserMapper.toUserDto(userStorage.save(user));
    }

    @Override
    public UserDto getById(Long userId) {
        return UserMapper.toUserDto(getUserOrThrow(userId));
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteById(Long userId) {
        getUserOrThrow(userId);
        userStorage.deleteById(userId);
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
    }

    private void assertEmailIsFree(String email, Long currentUserId) {
        boolean emailTaken = userStorage.findByEmail(email)
                .filter(existing -> currentUserId == null || !existing.getId().equals(currentUserId))
                .isPresent();
        if (emailTaken) {
            throw new ConflictException("Пользователь с email " + email + " уже существует");
        }
    }
}
