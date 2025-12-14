package vendingmachine.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vendingmachine.model.MintStatusMessage;

@Service
public class MintStatusPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public MintStatusPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void send(String policyId, MintStatusMessage msg) {
        messagingTemplate.convertAndSend(
                "/topic/mint-status/" + policyId,
                msg
        );
    }
}
