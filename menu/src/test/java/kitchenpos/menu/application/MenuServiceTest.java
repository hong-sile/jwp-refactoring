package kitchenpos.menu.application;

import static kitchenpos.fixture.MenuFixture.createMenuProductDto;
import static kitchenpos.fixture.MenuGroupFixture.한마리메뉴_DTO;
import static kitchenpos.menu.domain.exception.MenuExceptionType.PRICE_IS_BIGGER_THAN_MENU_PRODUCT_PRICES_SUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.List;
import kitchenpos.ServiceIntegrationTest;
import kitchenpos.fixture.MenuFixture;
import kitchenpos.fixture.ProductFixture;
import kitchenpos.menu.application.dto.MenuDto;
import kitchenpos.menu.application.dto.MenuProductDto;
import kitchenpos.menu.domain.exception.MenuException;
import kitchenpos.menu_group.application.MenuGroupDto;
import kitchenpos.menu_group.application.MenuGroupService;
import kitchenpos.product.application.ProductService;
import kitchenpos.product.application.dto.ProductDto;
import kitchenpos.vo.exception.PriceException;
import kitchenpos.vo.exception.PriceExceptionType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MenuServiceTest extends ServiceIntegrationTest {

    @Autowired
    private ProductService productService;
    @Autowired
    private MenuGroupService menuGroupService;
    @Autowired
    private MenuService menuService;

    @Nested
    @DisplayName("Menu를 생성한다.")
    class Create {

        @Test
        @DisplayName("정상적으로 생성한다.")
        void success() {
            final ProductDto savedProduct = productService.create(ProductFixture.후라이드_DTO());
            final MenuProductDto menuProductDto = createMenuProductDto(savedProduct,
                1L);
            final MenuGroupDto savedMenuGroupDto = menuGroupService.create(한마리메뉴_DTO());
            final MenuDto menuDto = MenuFixture.후라이드치킨_DTO(
                savedMenuGroupDto, List.of(menuProductDto), BigDecimal.valueOf(16000)
            );

            final MenuDto savedMenuDto = menuService.create(menuDto);

            assertAll(
                () -> assertThat(savedMenuDto)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "menuProducts.seq", "menuProducts.menuId", "price")
                    .isEqualTo(menuDto),
                () -> assertThat(savedMenuDto.getPrice())
                    .isEqualByComparingTo(menuDto.getPrice())
            );
        }

        @Test
        @DisplayName("가격이 0미만인 경우 예외처리.")
        void throwExceptionPriceLowerThan0() {
            final ProductDto savedProductDto = productService.create(ProductFixture.후라이드_DTO());
            final MenuProductDto menuProductDto = createMenuProductDto(savedProductDto,
                1L);
            final MenuGroupDto savedMenuGroupDto = menuGroupService.create(한마리메뉴_DTO());
            final MenuDto menuDto = MenuFixture.후라이드치킨_DTO(
                savedMenuGroupDto, List.of(menuProductDto), BigDecimal.valueOf(-16000)
            );

            //when
            Assertions.assertThatThrownBy(() -> menuService.create(menuDto))
                .isInstanceOf(PriceException.class)
                .hasMessage(PriceExceptionType.PRICE_IS_LOWER_THAN_ZERO.getMessage());
        }

        @Test
        @DisplayName("price가 product의 총합보다 큰 경우 예외처리")
        void throwExceptionPriceIsBiggerThanProductSum() {
            final ProductDto savedProduct = productService.create(ProductFixture.후라이드_DTO());
            final MenuProductDto menuProductDto = createMenuProductDto(savedProduct,
                1L);
            final MenuGroupDto savedMenuGroupDto = menuGroupService.create(한마리메뉴_DTO());
            final MenuDto menuDto = MenuFixture.후라이드치킨_DTO(
                savedMenuGroupDto, List.of(menuProductDto), BigDecimal.valueOf(18000)
            );

            //when
            Assertions.assertThatThrownBy(() -> menuService.create(menuDto))
                .isInstanceOf(MenuException.class)
                .hasMessage(PRICE_IS_BIGGER_THAN_MENU_PRODUCT_PRICES_SUM.getMessage());
        }
    }

    @Test
    @DisplayName("menu list를 조회한다.")
    void list() {
        final ProductDto savedProduct = productService.create(ProductFixture.후라이드_DTO());
        final MenuProductDto menuProductDto = createMenuProductDto(savedProduct, 1L);
        final MenuGroupDto savedMenuGroupDto = menuGroupService.create(한마리메뉴_DTO());
        final MenuDto menuDto = MenuFixture.후라이드치킨_DTO(
            savedMenuGroupDto, List.of(menuProductDto), BigDecimal.valueOf(16000)
        );

        final MenuDto savedMenuDto = menuService.create(menuDto);

        //when
        final List<MenuDto> menuDtos = menuService.list();

        assertAll(
            () -> Assertions.assertThat(menuDtos)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("price")
                .containsExactly(savedMenuDto),
            () -> assertThat(menuDtos.get(0).getPrice())
                .isEqualByComparingTo(savedMenuDto.getPrice())
        );
    }
}