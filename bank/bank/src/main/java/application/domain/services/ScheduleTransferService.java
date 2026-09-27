package application.domain.services;

import application.domain.valueobjects.AccountNumber;
import application.domain.valueobjects.Money;

public class ScheduleTransferService {
    public void schedule(AccountNumber source, AccountNumber destination, Money amount, String scheduleDate) {
        if (source == null || destination == null || amount == null || scheduleDate == null) {
            throw new IllegalArgumentException("Invalid scheduling data");
        }
    }
}
