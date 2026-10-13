package ru.practicum.shareit.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

@SpringBootTest
@Transactional
class ItemRequestServiceIntegrationTest {
    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;

    @Test
    void createGetOwnGetAllAndGetById() {
        long requestorId = user("Requestor", "requestor@mail.com");
        long otherId = user("Other", "other@mail.com");
        long ownerId = user("Owner", "owner-request@mail.com");

        ItemRequestDto older = itemRequestService.create(requestorId, request("older need"));
        ItemRequestDto newer = itemRequestService.create(requestorId, request("newer need"));
        ItemRequestDto foreign = itemRequestService.create(otherId, request("foreign need"));

        ItemRequest olderEntity = itemRequestRepository.findById(older.getId()).orElseThrow();
        olderEntity.setCreated(LocalDateTime.now().minusDays(1));
        ItemRequest newerEntity = itemRequestRepository.findById(newer.getId()).orElseThrow();
        newerEntity.setCreated(LocalDateTime.now());

        ItemDto withoutRequest = itemService.create(ownerId, item("Saw", null));
        assertThat(withoutRequest.getRequestId()).isNull();

        ItemDto first = itemService.create(ownerId, item("Drill", newer.getId()));
        ItemDto second = itemService.create(ownerId, item("Hammer", newer.getId()));
        assertThat(first.getRequestId()).isEqualTo(newer.getId());
        assertThat(second.getRequestId()).isEqualTo(newer.getId());

        assertThat(newer.getDescription()).isEqualTo("newer need");
        assertThat(newer.getCreated()).isNotNull();
        assertThat(newer.getItems()).isEmpty();

        assertThat(itemRequestService.getOwn(requestorId))
                .extracting(ItemRequestDto::getId)
                .containsExactly(newer.getId(), older.getId());
        ItemRequestDto ownWithItems = itemRequestService.getOwn(requestorId).get(0);
        assertThat(ownWithItems.getItems()).extracting("id").containsExactly(first.getId(), second.getId());
        assertThat(ownWithItems.getItems().get(0).getName()).isEqualTo("Drill");
        assertThat(ownWithItems.getItems().get(0).getOwnerId()).isEqualTo(ownerId);

        assertThat(itemRequestService.getAll(requestorId))
                .extracting(ItemRequestDto::getId)
                .contains(foreign.getId())
                .doesNotContain(newer.getId(), older.getId());

        ItemRequestDto seenByOther = itemRequestService.getById(otherId, newer.getId());
        assertThat(seenByOther.getItems()).extracting("name").containsExactly("Drill", "Hammer");
        assertThat(seenByOther.getItems().get(0).getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    void createItemWithUnknownRequestIsNotFound() {
        long ownerId = user("Owner2", "owner2-request@mail.com");
        ItemDto dto = item("Unknown", 9999L);
        assertThatThrownBy(() -> itemService.create(ownerId, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void missingUserOrRequest() {
        long userId = user("Alone", "alone-request@mail.com");
        ItemRequestDto missingUser = request("need");
        assertThatThrownBy(() -> itemRequestService.create(9999L, missingUser))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> itemRequestService.getOwn(9999L))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> itemRequestService.getAll(9999L))
                .isInstanceOf(NotFoundException.class);
        assertThat(itemRequestService.getOwn(userId)).isEmpty();
        ItemRequestDto created = itemRequestService.create(userId, request("real"));
        assertThat(ItemRequestMapper.toDto(itemRequestRepository.findById(created.getId()).orElseThrow(), null)
                .getItems()).isEmpty();
        assertThatThrownBy(() -> itemRequestService.getById(9999L, created.getId()))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> itemRequestService.getById(userId, 9999L))
                .isInstanceOf(NotFoundException.class);
    }

    private long user(String name, String email) {
        return userService.create(UserDto.builder().name(name).email(email).build()).getId();
    }

    private ItemRequestDto request(String description) {
        return ItemRequestDto.builder().description(description).build();
    }

    private ItemDto item(String name, Long requestId) {
        return ItemDto.builder()
                .name(name)
                .description(name + " description")
                .available(true)
                .requestId(requestId)
                .build();
    }
}
