package application.domain.services;

public class CancelScheduledTransferService {
    public void cancel(String transferId) {
        if (transferId == null || transferId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transfer ID cannot be empty");
        }
    }
}
