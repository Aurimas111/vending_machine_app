package vendingmachine.model;

public record RefundStatusMessage(
        String policyId,
        RefundStatus status,
        String txHash,
        String message
) {}
