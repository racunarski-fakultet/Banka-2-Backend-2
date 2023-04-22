package com.raf.si.Banka2Backend.controllers;

import com.raf.si.Banka2Backend.dto.OptionBuyDto;
import com.raf.si.Banka2Backend.dto.OptionSellDto;
import com.raf.si.Banka2Backend.dto.SellStockUsingOptionDto;
import com.raf.si.Banka2Backend.exceptions.StockNotFoundException;
import com.raf.si.Banka2Backend.exceptions.TooLateToBuyOptionException;
import com.raf.si.Banka2Backend.services.OptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.text.ParseException;

import com.raf.si.Banka2Backend.exceptions.OptionNotFoundException;
import com.raf.si.Banka2Backend.exceptions.UserNotFoundException;
import com.raf.si.Banka2Backend.models.mariadb.User;
import com.raf.si.Banka2Backend.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@RestController
@CrossOrigin
@RequestMapping("/api/options")
public class OptionController {

    private OptionService optionService;
    private final UserService userService;


    @Autowired
    public OptionController(OptionService optionService, UserService userService) {
        this.optionService = optionService;
        this.userService = userService;
    }

    @GetMapping("/{symbol}/{dateString}")
    public ResponseEntity<?> getStockBySymbol(@PathVariable String symbol, @PathVariable String dateString) throws ParseException {
        return ResponseEntity.ok().body(optionService.findByStockAndDate(symbol, dateString));
    }
    @GetMapping("/{symbol}")
    public ResponseEntity<?> getStockBySymbol(@PathVariable String symbol) throws ParseException {
        return ResponseEntity.ok().body(optionService.findByStock(symbol));
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellOption(@RequestBody OptionSellDto optionSellDto) {

        try{
            return ResponseEntity.ok().body(optionService.sellOption(optionSellDto.getUserOptionId(), optionSellDto.getPremium()));
        } catch(UserNotFoundException | OptionNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buyOption(@RequestBody OptionBuyDto optionBuyDto) {

        String signedInUserEmail = getContext().getAuthentication().getName();
        try{
            Optional<User> userOptional = userService.findByEmail(signedInUserEmail);

            return ResponseEntity.ok().body(optionService.buyOption(optionBuyDto.getOptionId(), userOptional.get().getId(), optionBuyDto.getAmount(), optionBuyDto.getPremium()));
        } catch(UserNotFoundException | OptionNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    @GetMapping("/buy-stocks/{userOptionId}")
    public ResponseEntity<?> buyStocksByOption(@PathVariable Long userOptionId) {

        String signedInUserEmail = getContext().getAuthentication().getName();

        try{
            Optional<User> userOptional = userService.findByEmail(signedInUserEmail);

            return ResponseEntity.ok().body(optionService.buyStockUsingOption(userOptionId, userOptional.get().getId()));
        } catch(UserNotFoundException | OptionNotFoundException | StockNotFoundException | TooLateToBuyOptionException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }

    }

    @PostMapping("/sell-stocks")
    public ResponseEntity<?> sellStocksByOption(@RequestBody SellStockUsingOptionDto sellStockUsingOptionDto) {

        //TODO Zavrsiti sell-stocks
        //Problem na koji sam naisao je to sto treba da se kreira UserOption, ali nemam podatak o optionId-ju
        //Moguce resenje je dozvoliti null vrednosti za optionId u UserOption modelu i migracionoj skripti
        //Znaci setovati samo userId - id onoga ko kreira SellStockUsingOption, a da optionId ostane null
        return null;
    }
}