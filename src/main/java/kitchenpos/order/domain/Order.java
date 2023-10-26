package kitchenpos.order.domain;

import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;
import static kitchenpos.order.domain.exception.OrderExceptionType.ORDER_IS_ALREADY_COMPLETION;

import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import kitchenpos.order.domain.exception.OrderException;

@Entity(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private Long orderTableId;

    @Enumerated(value = STRING)
    private OrderStatus orderStatus;

    private LocalDateTime orderedTime;

    @OneToMany(cascade = PERSIST)
    @JoinColumn(
        name = "order_id",
        updatable = false, nullable = false
    )
    private List<OrderLineItem> orderLineItems;

    protected Order() {
    }

    public Order(
        final Long orderTableId,
        final OrderStatus orderStatus,
        final LocalDateTime orderedTime,
        final List<OrderLineItem> orderLineItems
    ) {
        this.orderTableId = orderTableId;
        this.orderStatus = orderStatus;
        this.orderedTime = orderedTime;
        this.orderLineItems = orderLineItems;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderTableId() {
        return orderTableId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public LocalDateTime getOrderedTime() {
        return orderedTime;
    }

    public List<OrderLineItem> getOrderLineItems() {
        return orderLineItems;
    }

    public boolean isAlreadyCompletion() {
        return this.orderStatus == OrderStatus.COMPLETION;
    }

    public boolean isNotAlreadyCompletion() {
        return !isAlreadyCompletion();
    }

    public void changeOrderStatus(final OrderStatus orderStatus) {
        if (isAlreadyCompletion()) {
            throw new OrderException(ORDER_IS_ALREADY_COMPLETION);
        }
        this.orderStatus = orderStatus;
    }
}
