package com.github.holly.accountability.wallet;

import com.github.holly.accountability.purchase.Purchase;
import com.github.holly.accountability.purchase.PurchaseDto;
import com.github.holly.accountability.purchase.PurchaseService;
import com.github.holly.accountability.user.AccountabilitySessionUser;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/api/wallet")
@ResponseBody
public class WalletController {

    private final WalletService walletService;

    private final PurchaseService purchaseService;

    public WalletController(WalletService walletService, PurchaseService purchaseService) {
        this.walletService = walletService;
        this.purchaseService = purchaseService;
    }

    @GetMapping("")
    public WalletDto getWallet(@AuthenticationPrincipal AccountabilitySessionUser user){

        Wallet userWallet = walletService.findWalletByUserId(user.getId());
        return convertWalletToWalletDto(userWallet);
    }

    @GetMapping("/get-wallets")
    public Page<WalletDto> getWallets(@RequestParam(required = false) List<Long> userIds,
                                     @PageableDefault(size=20) Pageable pageable){

        return walletService.findWalletsByUserIds(userIds, pageable)
                .map(this::convertWalletToWalletDto);
    }

    @GetMapping("/getPurchases")
    public Page<PurchaseDto> getPurchases(@AuthenticationPrincipal AccountabilitySessionUser user,
                                          @PageableDefault() Pageable pageable){

        return purchaseService.getPurchasesByUserIdDescTime(user.getId(), pageable)
                .map(this::convertPurchaseToDto);
    }

    @PostMapping("/makePurchase")
    public PurchaseDto purchase(@AuthenticationPrincipal AccountabilitySessionUser user,
                                @RequestBody PurchaseDto purchaseDto){
        Purchase purchase = purchaseService.makePurchase(user.getId(), purchaseDto.getPrice(), purchaseDto.getDescription());

        if (purchase == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        return convertPurchaseToDto(purchase);
    }

    private PurchaseDto convertPurchaseToDto(Purchase purchase){
        PurchaseDto purchaseDto = new PurchaseDto();
        purchaseDto.setId(purchase.getId());
        purchaseDto.setDescription(purchase.getDescription());
        purchaseDto.setPrice(purchase.getPrice());
        if (purchase.getPurchaseTime() != null) {
            LocalDateTime purchaseTime = purchase.getPurchaseTime();
            String time = purchaseTime.toString();
            purchaseDto.setPurchaseTimeString(time);
        }
        return purchaseDto;
    }

    private WalletDto convertWalletToWalletDto(Wallet wallet){
        WalletDto walletDto = new WalletDto();
        walletDto.setBalance(wallet.getBalance());
        walletDto.setUserId(wallet.getUser().getId());
        walletDto.setUserName(wallet.getUser().getUsername());
        return walletDto;
    }
}