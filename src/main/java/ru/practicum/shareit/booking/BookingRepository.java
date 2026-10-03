package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByItemIdAndStatus(Long itemId, BookingStatus status);

    List<Booking> findByItemIdInAndStatus(Collection<Long> itemIds, BookingStatus status);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId,
                                                            Long itemId,
                                                            BookingStatus status,
                                                            LocalDateTime end);
}
