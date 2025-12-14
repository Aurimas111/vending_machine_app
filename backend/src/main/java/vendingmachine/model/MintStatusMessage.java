package vendingmachine.model;

public record MintStatusMessage(
        String policyId,
        MintStatus status,
        String txHash,
        String message
) {}