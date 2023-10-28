package kitchenpos.table.domain.implementation;

import static kitchenpos.table_group.domain.exception.TableGroupExceptionType.ORDER_TABLE_IS_NOT_EMPTY;
import static kitchenpos.table_group.domain.exception.TableGroupExceptionType.ORDER_TABLE_SIZE_IS_LOWER_THAN_TWO;

import java.util.List;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.OrderTableRepository;
import kitchenpos.table_group.domain.GroupTableValidator;
import kitchenpos.table_group.domain.exception.TableGroupException;
import kitchenpos.table_group.domain.exception.TableGroupExceptionType;
import org.springframework.stereotype.Component;

@Component
public class TableGroupTableValidator implements GroupTableValidator {

    private static final int MINIMUM_TABLE_SIZE = 2;

    private final OrderTableRepository orderTableRepository;

    public TableGroupTableValidator(final OrderTableRepository orderTableRepository) {
        this.orderTableRepository = orderTableRepository;
    }

    @Override
    public void validateGroupTable(final List<Long> orderTableIds) {
        final List<OrderTable> savedOrderTables = orderTableRepository.findAllByIdIn(orderTableIds);
        validateRequestTableAlreadySaved(orderTableIds, savedOrderTables);
        validateOrderTableSize(savedOrderTables);
        validateTableGroupEmpty(savedOrderTables);
    }

    private void validateTableGroupEmpty(final List<OrderTable> savedOrderTables) {
        final boolean isNotEmpty = savedOrderTables.stream()
            .anyMatch(orderTable -> !orderTable.isEmpty());

        if (isNotEmpty) {
            throw new TableGroupException(ORDER_TABLE_IS_NOT_EMPTY);
        }
    }

    private static void validateOrderTableSize(final List<OrderTable> savedOrderTables) {
        if (savedOrderTables.size() < MINIMUM_TABLE_SIZE) {
            throw new TableGroupException(ORDER_TABLE_SIZE_IS_LOWER_THAN_TWO);
        }
    }

    private static void validateRequestTableAlreadySaved(
        final List<Long> orderTableIds,
        final List<OrderTable> savedOrderTables
    ) {
        if (orderTableIds.size() != savedOrderTables.size()) {
            throw new TableGroupException(TableGroupExceptionType.ORDER_TABLE_IS_NOT_PRESENT_ALL);
        }
    }
}