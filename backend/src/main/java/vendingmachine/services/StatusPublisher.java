package vendingmachine.services;

import vendingmachine.model.MintStatusMessage;
import vendingmachine.model.RefundStatusMessage;

public interface StatusPublisher {
    void sendMintStatus(String wallet, MintStatusMessage msg);
    void sendRefundStatus(String wallet, RefundStatusMessage msg);
}
