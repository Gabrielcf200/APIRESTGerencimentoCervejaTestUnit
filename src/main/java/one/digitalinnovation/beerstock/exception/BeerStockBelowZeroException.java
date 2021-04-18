package one.digitalinnovation.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerStockBelowZeroException extends Exception {

    public BeerStockBelowZeroException(Long id, int quantityToIncrement) {
        super(String.format("Beers with %s ID to decremento informed exceeds the minimum stock capacity: %s", id, quantityToIncrement));
    }
}
