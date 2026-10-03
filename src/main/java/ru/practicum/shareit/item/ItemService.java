package ru.practicum.shareit.item;

import java.util.List;
import ru.practicum.shareit.item.dto.ItemDto;

public interface ItemService {

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemDto getById(Long userId, Long itemId);

    List<ItemDto> getAllByOwner(Long userId);

    List<ItemDto> search(Long userId, String text);
}
