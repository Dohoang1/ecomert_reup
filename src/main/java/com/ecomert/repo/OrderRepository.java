package com.ecomert.repo;

import com.ecomert.model.Order;
import com.ecomert.model.OrderStatus;
import com.ecomert.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    //List<Order> findByUserOrderByOrderTimeDesc(User user);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    long countByStatus(OrderStatus status);
    @Query("SELECT o FROM Order o WHERE o.user = :user ORDER BY o.orderTime DESC")
    List<Order> findByUserOrderByOrderTimeDesc(@Param("user") User user);

}