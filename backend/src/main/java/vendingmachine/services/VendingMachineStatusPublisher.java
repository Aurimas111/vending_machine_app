package vendingmachine.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vendingmachine.model.MintStatusMessage;
import vendingmachine.model.RefundStatusMessage;

@Service
public class VendingMachineStatusPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public VendingMachineStatusPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // should update the topic to not use wallet address, since its easy to guess
    // a good solution would be to use user id instead

    public void send(String walletAddress, MintStatusMessage msg) {
        messagingTemplate.convertAndSend(
                "/topic/vending-machine-status/" + walletAddress,
                msg
        );
    }

    public void send(String walletAddress, RefundStatusMessage msg) {
        messagingTemplate.convertAndSend(
                "/topic/vending-machine-status/" + walletAddress,
                msg
        );
    }
}
