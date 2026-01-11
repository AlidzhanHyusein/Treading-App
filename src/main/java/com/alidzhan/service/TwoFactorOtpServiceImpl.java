package com.alidzhan.service;

import com.alidzhan.modal.TwoFactorOTP;
import com.alidzhan.modal.User;
import com.alidzhan.repository.TwoFactorRepo;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class TwoFactorOtpServiceImpl implements TwoFactorOtpService{
    private final TwoFactorRepo twoFactorRepo;

    public TwoFactorOtpServiceImpl(TwoFactorRepo twoFactorRepo) {
        this.twoFactorRepo = twoFactorRepo;
    }

    @Override
    public TwoFactorOTP createTwoFactorOtp(User user, String otp, String jwt) {
        UUID uuid = UUID.randomUUID();
        String id = uuid.toString();

        TwoFactorOTP twoFactorOTP = new TwoFactorOTP();

        twoFactorOTP.setOtp(otp);
        twoFactorOTP.setId(id);
        twoFactorOTP.setJwt(jwt);
        twoFactorOTP.setUser(user);

        return twoFactorRepo.save(twoFactorOTP);
    }

    @Override
    public TwoFactorOTP findByUser(Long userId) {
        return twoFactorRepo.findByUserId(userId);
    }

    @Override
    public TwoFactorOTP findById(String id) {
        Optional<TwoFactorOTP> opt = twoFactorRepo.findById(id);
        return opt.orElse(null);
    }

    @Override
    public boolean verifyTwoFactorOtp(TwoFactorOTP twoFactorOTP, String otp) {
        return twoFactorOTP.getOtp().equals(otp);
    }

    @Override
    public void deleteTwoFactorOtp(TwoFactorOTP twoFactorOTP) {
        twoFactorRepo.delete(twoFactorOTP);
    }
}
