package one.digitalinnovation.beerstock.service;

import lombok.AllArgsConstructor;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockBelowZeroException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BeerService {


    private final BeerRepository beerRepository;

    private final BeerMapper beerMapper = BeerMapper.INSTANCE;

    public BeerDTO createBeer(BeerDTO beerDTO) throws BeerAlreadyRegisteredException {
        verifyIfIsAlreadyRegistered(beerDTO.getName());
        Beer beer = beerMapper.toModel(beerDTO);
        Beer savedBeer = beerRepository.save(beer);
        return beerMapper.toDTO(savedBeer);
    }

    public BeerDTO findByName(String name) throws BeerNotFoundException {
        Beer foundBeer = beerRepository.findByName(name)
                .orElseThrow(() -> new BeerNotFoundException(name));
        return beerMapper.toDTO(foundBeer);
    }

    public List<BeerDTO> listAll() {
        return beerRepository.findAll()
                .stream()
                .map(beerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) throws BeerNotFoundException {
        verifyIfExists(id);
        beerRepository.deleteById(id);
    }

    private void verifyIfIsAlreadyRegistered(String name) throws BeerAlreadyRegisteredException {
        Optional<Beer> optSavedBeer = beerRepository.findByName(name);
        if (optSavedBeer.isPresent()) {
            throw new BeerAlreadyRegisteredException(name);
        }
    }

    private Beer verifyIfExists(Long id) throws BeerNotFoundException {
        return beerRepository.findById(id)
                .orElseThrow(() -> new BeerNotFoundException(id));
    }

    public BeerDTO increment(Long id, int quantityToIncrement) throws BeerNotFoundException, BeerStockExceededException {
        Beer beerToIncrementStock = verifyIfExists(id);
        int quantityAfterIncrement = quantityToIncrement + beerToIncrementStock.getQuantity();
        if (quantityAfterIncrement <= beerToIncrementStock.getMax()) {
            beerToIncrementStock.setQuantity(quantityAfterIncrement);
            Beer incrementedBeerStock = beerRepository.save(beerToIncrementStock);
            return beerMapper.toDTO(incrementedBeerStock);
        }
        throw new BeerStockExceededException(id, quantityToIncrement);
    }

    public BeerDTO decrement(Long id, int quantityToDecrement) throws BeerNotFoundException, BeerStockBelowZeroException {
       /* 1- Tentei elaborar um c??digo simples para passar nos testes, mas n??o era o suficiente para deixar o m??todo perfeito
          2- Utilizei o m??todo verifyIfExists que retorna um objeto Beer, coloquei dentro de uma vari??vel para ser decrementado
          3- Como o stock n??o pode ficar abaixo de zero, criei uma vari??vel(quantityAfterDecrement) recebendo o valor
          do stock subtra??do pelo valor de quantityToDecrement, se esse valor fosse maior ou igual ao zero ent??o poderia
           continuar o decremento.
          4- Se isso n??o ocorrer criei uma exce????o chamada BeerStockBelowZeroException para informar que o valor que
          est?? sendo decrementado vai deixar o stock abaixo de zero, impossibilitando essa execu????o do m??todo.
          5-"Setei" o valor de quantity sendo quantityAfterDecrement, depois eu utilizei o m??todo save do beerRepository
          para salvar a cerveja e armazenando na vari??vel decrementBeerStock, que ser?? retornada sendo transformada pelo
          m??todo toDTO do beerMapper em uma beerDTO.

        */
        Beer beerToDecrementStock = verifyIfExists(id);
        int quantityAfterDecrement = beerToDecrementStock.getQuantity() - quantityToDecrement;
        if(quantityAfterDecrement >= 0){
            beerToDecrementStock.setQuantity(quantityAfterDecrement);
            Beer decrementBeerStock = beerRepository.save(beerToDecrementStock);
            return beerMapper.toDTO(decrementBeerStock);
        }
        throw new BeerStockBelowZeroException(id, quantityToDecrement);


    }
}
