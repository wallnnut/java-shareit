package ru.practicum.shareit.item;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByItemIdOrderByCreatedAsc(Long itemId);

    List<Comment> findByItemIdInOrderByCreatedAsc(Collection<Long> itemIds);
}
