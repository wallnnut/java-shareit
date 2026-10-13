package ru.practicum.shareit.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        User requestor = getUserOrThrow(userId);
        ItemRequest request = ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
        return ItemRequestMapper.toDto(itemRequestRepository.save(request), List.of());
    }

    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        getUserOrThrow(userId);
        return toDtos(itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId));
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId) {
        getUserOrThrow(userId);
        return toDtos(itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId));
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        getUserOrThrow(userId);
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден: " + requestId));
        return toDto(request);
    }

    private ItemRequestDto toDto(ItemRequest request) {
        return ItemRequestMapper.toDto(request, itemRepository.findByRequestIdIn(List.of(request.getId())));
    }

    private List<ItemRequestDto> toDtos(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();
        Map<Long, List<Item>> itemsByRequest = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        return requests.stream()
                .map(request -> ItemRequestMapper.toDto(request,
                        itemsByRequest.getOrDefault(request.getId(), List.of())))
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
    }
}
