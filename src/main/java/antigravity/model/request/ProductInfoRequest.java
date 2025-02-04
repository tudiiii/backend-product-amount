package antigravity.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProductInfoRequest {

    private int productId;
    private int[] couponIds;

}
