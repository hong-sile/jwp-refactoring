package kitchenpos.application;

import static kitchenpos.fixture.MenuFixture.createMenuProduct;
import static kitchenpos.fixture.MenuFixture.한마리메뉴;
import static kitchenpos.fixture.MenuFixture.후라이드치킨;
import static kitchenpos.fixture.ProductFixture.후라이드;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.List;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MenuServiceTest extends ServiceIntegrationTest {

    @Nested
    @DisplayName("Menu를 생성한다.")
    class create {

        @Test
        @DisplayName("정상적으로 생성한다.")
        void create() {
            final Product savedProduct = productService.create(후라이드());
            final MenuProduct menuProduct = createMenuProduct(savedProduct, 1L);
            final MenuGroup savedMenuGroup = menuGroupService.create(한마리메뉴());
            final Menu menu = 후라이드치킨(savedMenuGroup, List.of(menuProduct));

            final Menu savedMenu = menuService.create(menu);

            assertAll(
                () -> assertThat(savedMenu)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "menuProducts.seq", "price")
                    .isEqualTo(menu),
                () -> assertThat(savedMenu.getPrice())
                    .isEqualByComparingTo(menu.getPrice())
            );
        }

        @Test
        @DisplayName("가격이 0미만인 경우 예외처리.")
        void throwExceptionPriceLowerThan0() {
            final Product savedProduct = productService.create(후라이드());
            final MenuProduct menuProduct = createMenuProduct(savedProduct, 1L);
            final MenuGroup savedMenuGroup = menuGroupService.create(한마리메뉴());
            final Menu menu = 후라이드치킨(savedMenuGroup, List.of(menuProduct));

            menu.setPrice(BigDecimal.valueOf(-1000));

            //when
            assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
        }


        @Test
        @DisplayName("MenuGroup이 저장되지 않은 경우 예외처리")
        void throwExceptionMenuGroupIsNotExist() {
            final Product savedProduct = productService.create(후라이드());
            final MenuProduct menuProduct = createMenuProduct(savedProduct, 1L);
            final MenuGroup unSavedMenuGroup = 한마리메뉴();
            final Menu menu = 후라이드치킨(unSavedMenuGroup, List.of(menuProduct));

            //when
            assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("product가 저장되지 않은 경우 예외처리")
        void throwExceptionProductIsNotExist() {
            final Product unSavedProduct = 후라이드();
            final MenuProduct menuProduct = createMenuProduct(unSavedProduct, 1L);
            final MenuGroup savedMenuGroup = menuGroupService.create(한마리메뉴());
            final Menu menu = 후라이드치킨(savedMenuGroup, List.of(menuProduct));

            //when
            assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("price가 product의 총합보다 큰 경우 예외처리")
        void throwExceptionPriceIsBiggerThanProductSum() {
            final Product savedProduct = productService.create(후라이드());
            final MenuProduct menuProduct = createMenuProduct(savedProduct, 1L);
            final MenuGroup savedMenuGroup = menuGroupService.create(한마리메뉴());

            final Menu menu = 후라이드치킨(savedMenuGroup, List.of(menuProduct));
            menu.setPrice(BigDecimal.valueOf(18000));

            //when
            assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    @DisplayName("menu list를 조회한다.")
    void list() {
        final Product savedProduct = productService.create(후라이드());
        final MenuProduct menuProduct = createMenuProduct(savedProduct, 1L);
        final MenuGroup savedMenuGroup = menuGroupService.create(한마리메뉴());
        final Menu menu = 후라이드치킨(savedMenuGroup, List.of(menuProduct));

        final Menu savedMenu = menuService.create(menu);

        //when
        final List<Menu> menus = menuService.list();

        assertAll(
            () -> assertThat(menus)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("price")
                .containsExactly(savedMenu),
            () -> assertThat(menus.get(0).getPrice())
                .isEqualByComparingTo(savedMenu.getPrice())
        );
    }
}
