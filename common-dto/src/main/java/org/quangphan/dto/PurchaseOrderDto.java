package org.quangphan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class PurchaseOrderDto {

    private Long orderId;
    private Integer productId;
    private Integer price;
    private Integer userId;

}
