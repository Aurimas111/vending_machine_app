package vendingmachine.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundDataDTO {
    private String refundAddress;
    private int amount;
    private String txHash;

    public RefundDataDTO(String refundAddress, int amount, String txHash) {
        this.refundAddress = refundAddress;
        this.amount = amount;
        this.txHash = txHash;
    }

    public RefundDataDTO() {
    }
}
