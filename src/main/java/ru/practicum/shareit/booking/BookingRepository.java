package ru.practicum.shareit.booking;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqual(Long bookerId,
                                                                             LocalDateTime start,
                                                                             LocalDateTime end,
                                                                             Sort sort);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByItem_OwnerId(Long ownerId, Sort sort);

    List<Booking> findByItem_OwnerIdAndStartLessThanEqualAndEndGreaterThanEqual(Long ownerId,
                                                                                 LocalDateTime start,
                                                                                 LocalDateTime end,
                                                                                 Sort sort);

    List<Booking> findByItem_OwnerIdAndEndIsBefore(Long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findByItem_OwnerIdAndStartIsAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByItem_OwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    List<Booking> findByItemIdAndStatus(Long itemId, BookingStatus status);

    List<Booking> findByItemIdInAndStatus(Collection<Long> itemIds, BookingStatus status);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId,
                                                            Long itemId,
                                                            BookingStatus status,
                                                            LocalDateTime end);
}
