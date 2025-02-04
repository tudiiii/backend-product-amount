package antigravity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import antigravity.exception.ProductApplicationException;
import antigravity.exception.code.PromotionErrorCode;
import antigravity.fixture.ProductFixture;
import antigravity.fixture.PromotionProductFixture;
import antigravity.model.request.ProductInfoRequest;
import antigravity.repository.ProductRepository;
import antigravity.repository.PromotionProductsRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;


@SpringBootTest
@DisplayName("- 프로모션관련 EXCEPTION 테스트 케이스")
public class PromotionServiceExceptionTest {

    @Autowired private CalculatePriceService calculatePriceService;
    @MockBean private ProductRepository productRepository;
    @MockBean private PromotionProductsRepository promotionProductsRepository;

    @Test
    @DisplayName("[프로모션] 해당 프로모션이 존재하지 않는다면 ProductApplicationException(NOT_EXIST_PROMOTION)을 발생시킨다.")
    void promotionNotExistException() {
        ProductInfoRequest pir = ProductInfoRequest.builder().productId(1).couponIds(new int[]{1, 8}).build();

        given(productRepository.findById(1)).willReturn(Optional.ofNullable(ProductFixture.getProduct()));

        ProductApplicationException e = assertThrows(ProductApplicationException.class,
            () -> calculatePriceService.getProductAmount(pir));

        assertEquals(PromotionErrorCode.NOT_EXIST_PROMOTION, e.getErrorCode());
    }

    @Test
    @DisplayName("[프로모션] 해당 프로모션에 대한 입력값이 없다면 ProductApplicationException(NOT_EXIST_PROMOTION)을 발생시킨다.")
    void promotionRequestNotExistException() {
        ProductInfoRequest pir = ProductInfoRequest.builder().productId(1).couponIds(new int[]{}).build();

        given(productRepository.findById(1)).willReturn(Optional.ofNullable(ProductFixture.getProduct()));
        given(promotionProductsRepository.findWithPromotionByPromotionIdIn(any())).willReturn(List.of());

        ProductApplicationException e = assertThrows(ProductApplicationException.class,
            () -> calculatePriceService.getProductAmount(pir));

        assertEquals(PromotionErrorCode.NOT_EXIST_PROMOTION, e.getErrorCode());
    }


    @Test
    @DisplayName("[프로모션] 중복되는 프로모션 적용이 요청된다면 ProductApplicationException(DUPLICATED_PROMOTION)을 발생시킨다.")
    void promotionDuplicationException() {
        ProductInfoRequest pir = ProductInfoRequest.builder().productId(1).couponIds(new int[]{1, 1}).build();
        given(productRepository.findById(1)).willReturn(Optional.ofNullable(ProductFixture.getProduct()));

        ProductApplicationException e = assertThrows(ProductApplicationException.class,
            () -> calculatePriceService.getProductAmount(pir));

        assertEquals(PromotionErrorCode.DUPLICATED_PROMOTION, e.getErrorCode());
        then(productRepository).should().findById(any(Integer.class));
    }

    @Test
    @DisplayName("[프로모션] 해당 프로모션의 기한이 지났다면 ProductApplicationException(INVALID_PROMOTION_PERIOD)을 발생시킨다.")
    void promotionPeriodInvalidException() {
        ProductInfoRequest pir = ProductInfoRequest.builder().productId(1).couponIds(new int[]{5}).build();
        given(productRepository.findById(1)).willReturn(Optional.ofNullable(ProductFixture.getProduct()));
        given(promotionProductsRepository.findWithPromotionByPromotionIdIn(any())).willReturn(
            Collections.singletonList(PromotionProductFixture.getExpiredPeriodPromotionProducts()));

        ProductApplicationException e = assertThrows(ProductApplicationException.class,
            () -> calculatePriceService.getProductAmount(pir));

        assertEquals(PromotionErrorCode.INVALID_PROMOTION_PERIOD, e.getErrorCode());
    }

    @Test
    @DisplayName("[프로모션] 해당 프로모션의에 해당되는 상품이 아니라면 ProductApplicationException(INVALID_PROMOTION_PRODUCT)을 발생시킨다.")
    void promotionOfProductInvalidException() {
        ProductInfoRequest pir = ProductInfoRequest.builder().productId(1).couponIds(new int[]{1}).build();

        given(productRepository.findById(1)).willReturn(Optional.ofNullable(ProductFixture.getProduct()));
        given(promotionProductsRepository.findWithPromotionByPromotionIdIn(any())).willReturn(
            Collections.singletonList(PromotionProductFixture.getInValidPromotionProducts()));

        ProductApplicationException e = assertThrows(ProductApplicationException.class,
            () -> calculatePriceService.getProductAmount(pir));

        assertEquals(PromotionErrorCode.INVALID_PROMOTION_PRODUCT, e.getErrorCode());
    }

    @Test
    @DisplayName("[프로모션] 해당 프로모션 할인가격이 기존 제품 가격보다 초과된다면 ProductApplicationException(EXCEED_ORIGIN_PRICE)을 발생시킨다.")
    void promotionPriceExceedProductPriceException() {
        ProductInfoRequest pir = ProductInfoRequest.builder().productId(1).couponIds(new int[]{7}).build();

        given(productRepository.findById(1)).willReturn(Optional.ofNullable(ProductFixture.getProduct()));
        given(promotionProductsRepository.findWithPromotionByPromotionIdIn(any())).willReturn(
            Collections.singletonList(PromotionProductFixture.getExceedPromotionPriceProducts()));

        ProductApplicationException e = assertThrows(ProductApplicationException.class,
            () -> calculatePriceService.getProductAmount(pir));

        assertEquals(PromotionErrorCode.EXCEED_ORIGIN_PRICE, e.getErrorCode());
    }
}