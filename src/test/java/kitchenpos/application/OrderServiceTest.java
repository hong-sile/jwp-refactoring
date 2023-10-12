package kitchenpos.application;

import static kitchenpos.fixture.OrderFixture.createOrderLineItem;
import static kitchenpos.fixture.TableFixture.주문_테이블;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import kitchenpos.dao.OrderDao;
import kitchenpos.domain.Menu;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OrderServiceTest extends ServiceIntegrationTest {

    @Autowired
    private OrderDao orderDao;

    @Nested
    @DisplayName("order를 생성한다.")
    class Create {

        @Test
        @DisplayName("order를 성공적으로 생성한다.")
        void success() {
            //given
            final Menu menu = createMenu();
            final OrderLineItem orderLineItem = createOrderLineItem(menu.getId(), 1L);
            final OrderTable savedOrderTable = createOrderTable();

            final Order order = new Order();
            order.setOrderLineItems(List.of(orderLineItem));
            order.setOrderTableId(savedOrderTable.getId());

            //when
            final Order savedOrder = orderService.create(order);

            //then
            assertThat(savedOrder)
                .usingRecursiveComparison()
                .ignoringFields("id", "orderLineItems.seq")
                .isEqualTo(order);
        }

        @Test
        @DisplayName("orderLineItem에 있는 menu가 존재하지 않는 경우 예외처리")
        void throwExceptionOrderLineItemsIsEmpty() {
            //given
            final Menu menu = createMenu();
            final OrderLineItem orderLineItem = createOrderLineItem(menu.getId() + 1, 1L);
            final OrderTable savedOrderTable = createOrderTable();

            final Order order = new Order();
            order.setOrderLineItems(List.of(orderLineItem));
            order.setOrderTableId(savedOrderTable.getId());

            //when
            assertThatThrownBy(() -> orderService.create(order))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("orderTable이 비어있는 경우 예외처리")
        void throwExceptionOrderTableIsEmpty() {
            //given
            final Menu menu = createMenu();
            final OrderLineItem orderLineItem = createOrderLineItem(menu.getId(), 1L);
            final OrderTable savedOrderTable = tableService.create(주문_테이블());

            final Order order = new Order();
            order.setOrderLineItems(List.of(orderLineItem));
            order.setOrderTableId(savedOrderTable.getId());

            //when
            assertThatThrownBy(() -> orderService.create(order))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("order를 list로 조회한다.")
    void list() {
        //given
        final Order order = createOrderSuccessfully();

        //when
        final List<Order> foundOrders = orderService.list();

        //then
        assertThat(foundOrders)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields()
            .containsExactly(order);
    }

    @Nested
    @DisplayName("order의 상태를 바꾼다.")
    class ChangeOrderStatus {

        @Test
        @DisplayName("정상적으로 바꾼다.")
        void success() {
            //given
            final Order cookingOrder = createOrderSuccessfully();
            cookingOrder.setOrderStatus(OrderStatus.COMPLETION.name());

            //when
            orderService.changeOrderStatus(cookingOrder.getId(), cookingOrder);

            //then
            final Order foundOrder = orderDao.findById(cookingOrder.getId())
                .orElseThrow(RuntimeException::new);
            assertThat(foundOrder.getOrderStatus())
                .isEqualTo(OrderStatus.COMPLETION.name());
        }

        @Test
        @DisplayName("변경하려는 order의 상태가 completion인 경우 예외처리")
        void throwExceptionOrderStatusIsCompletion() {
            //given
            final Order cookingOrder = createOrderSuccessfully();
            cookingOrder.setOrderStatus(OrderStatus.COMPLETION.name());
            orderService.changeOrderStatus(cookingOrder.getId(), cookingOrder);

            //when
            cookingOrder.setOrderStatus(OrderStatus.MEAL.name());
            assertThatThrownBy(
                () -> orderService.changeOrderStatus(cookingOrder.getId(), cookingOrder)
            ).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
