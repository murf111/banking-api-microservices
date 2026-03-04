package com.portfolio.bank.user.api;

public interface LoginAttemptService {

    void loginFailed(String email);

    void loginSucceeded(String email);

    boolean isBlocked(String email);

    int getRemainingAttempts(String email);

    long getRemainingLockTime(String email);
}
