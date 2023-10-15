package kitchenpos.domain;

import static kitchenpos.domain.exception.OrderTableExceptionType.NUMBER_OF_GUEST_LOWER_THAN_ZERO;
import static kitchenpos.domain.exception.OrderTableExceptionType.TABLE_CANT_CHANGE_EMPTY_ALREADY_IN_GROUP;
import static kitchenpos.domain.exception.OrderTableExceptionType.TABLE_CANT_CHANGE_NUMBER_OF_GUESTS_EMPTY;
import static kitchenpos.fixture.TableFixture.비어있는_주문_테이블;
import static kitchenpos.fixture.TableFixture.비어있지_않는_주문_테이블;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kitchenpos.domain.exception.OrderTableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrderTableTest {

    @Test
    @DisplayName("OrderTable의 numberOfGuest가 0이하면 예외처리한다.")
    void validateNumberOfGuest() {
        assertThatThrownBy(() -> new OrderTable(1L, -1, true))
            .isInstanceOf(OrderTableException.class)
            .hasMessage(NUMBER_OF_GUEST_LOWER_THAN_ZERO.getMessage());
    }

    @Nested
    @DisplayName("orderTable의 empty 상태를 변경할 수 있다.")
    class ChangeEmpty {

        @Test
        @DisplayName("정상적으로 변경하는 경우")
        void success() {
            final OrderTable orderTable = new OrderTable(null, 0, true);

            orderTable.changeEmpty(false);

            assertThat(orderTable.isEmpty())
                .isEqualTo(false);
        }

        @Test
        @DisplayName("tableGroup에 속해있는 orderTalbe인 경우 예외처리 한다.")
        void throwExceptionTableGroupIdIsNull() {
            final OrderTable orderTable = new OrderTable(1L, 0, true);

            assertThatThrownBy(() -> orderTable.changeEmpty(false))
                .isInstanceOf(OrderTableException.class)
                .hasMessage(TABLE_CANT_CHANGE_EMPTY_ALREADY_IN_GROUP.getMessage());
        }
    }

    @Nested
    @DisplayName("numberOfGuest를 변경한다.")
    class ChangeNumberOfGuests {

        @Test
        @DisplayName("정상적으로 변경하는 경우")
        void success() {
            final OrderTable table = 비어있지_않는_주문_테이블();
            final int numberOfGuests = 10;

            table.changeNumberOfGuests(numberOfGuests);

            assertThat(table.getNumberOfGuests())
                .isEqualTo(10);
        }

        @Test
        @DisplayName("table이 empty인 경우 예외처리한다.")
        void throwExceptionTableIsEmpty() {
            final OrderTable emptyTable = 비어있는_주문_테이블();

            assertThatThrownBy(() -> emptyTable.changeNumberOfGuests(10))
                .isInstanceOf(OrderTableException.class)
                .hasMessage(TABLE_CANT_CHANGE_NUMBER_OF_GUESTS_EMPTY.getMessage());
        }
    }
}
