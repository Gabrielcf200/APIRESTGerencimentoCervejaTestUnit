package one.digitalinnovation.beerstock.service;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockBelowZeroException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = 1L;

    @Mock
    // Criação de um beerRepository mockado, simulando as funcionalidades desse obj
    private BeerRepository beerRepository;

    private BeerMapper beerMapper = BeerMapper.INSTANCE;

    @InjectMocks
    private BeerService beerService;

    @Test
        // When e Then utilizado no nome do teste para representar a condicação, ficando quando x então y
    void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {
        //given

        // Criando uma cerveja utilizando a classe BeerDTOBuilder, para ser a nossa cerveja esperada
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedSavedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        //Quando beerRepository chamar o método findByName do expectedBeerDTO.getName, então return um Optional.empty();
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
        //Quando beerRepository chamar om método save passe a expectedSavedBeer e retorna a mesma.
        when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);

        //then

        BeerDTO createdBearDTO = beerService.createBeer(expectedBeerDTO);

        //Confira se a createdBeerDTO.getId é igual a expectedBeerDTO.getid
        assertThat(createdBearDTO.getId(), is(equalTo(expectedBeerDTO.getId())));
        //Confira se a createdBeerDTO.getName é igual a expectedBeerDTO.getName
        assertThat(createdBearDTO.getName(), is(equalTo(expectedBeerDTO.getName())));
        //Confira se a createdBeerDTO.getQuantity é igual a expectedBeerDTO.getQuantity
        assertThat(createdBearDTO.getQuantity(), is(equalTo(expectedBeerDTO.getQuantity())));
        // confira se a createdBearDTO.getQuantity é maior que 2
        assertThat(createdBearDTO.getQuantity(), is(greaterThan(2)));

    }


    @Test
    void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {
        // given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        // when
        /*Quando beerRepository chamar o método findByName passando o Name do expectedFoundBeer, então retorne o
        expectedFoundBeer*/
        when(beerRepository.findByName(expectedFoundBeer.getName())).thenReturn(Optional.of(expectedFoundBeer));

        // then
        BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());
        // Confirme que o foundBeerDTO é igual ao expectedFoundBeerDTO
        assertThat(foundBeerDTO, is(equalTo(expectedFoundBeerDTO)));
    }

    //
    @Test
    void whenAlreadyRegisteredBeerInformedThenAnExceptionShouldBeThrown() throws BeerAlreadyRegisteredException {
        // given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);
        //when
        /*
        Quando o beerRepository chamar o método findByName passando o expectedBeerDTO.getName, então retorna Optional
        com o valor sendo duplicateBeer
         */

        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));
        /*Confira se há um exceção da BeerAlreadyRegisteredException.class, quando o beerService chamar o método createBeer
        passando expectedBeerDTO como valor.
        * */
        assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));

    }

    @Test
    void whenNotRegisteredBeerNameIsGivenThenThrowAnException() {
        // given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // when
        //Quando o beerRepository chamar o método findByName passando expectedFoundBeerDTO.getName, retorne um Optional vazio.
        when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());

        // then
        /*Confira se há uma exceção BeerNotFoundException, quando o beerService chamar o método findByName passando
        expectedFoundBeerDTO.getName().*/
        assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedFoundBeerDTO.getName()));
    }

    @Test
    void whenListBeerIsCalledThenReturnAListOfBeers() {
        // given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        //when
        //Quando beerRepository chamar o método findAll, então retorne uma lista com um único elemento sendo expectedFoundBeer
        when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundBeer));

        //then
        List<BeerDTO> foundListBeersDTO = beerService.listAll();
        //Confira se a foundListBeersDTO, não é vazia.
        assertThat(foundListBeersDTO, is(not(empty())));
        //Confira se o elemento 0 da foundListBeers é igual ao expectedFoundBeerDTO
        assertThat(foundListBeersDTO.get(0), is(equalTo(expectedFoundBeerDTO)));
    }

    @Test
    void whenListBeerIsCalledThenReturnAnEmptyListOfBeers() {
        //when
        //Quando beerRepository chamar o findALl, então retorne uma lista vazia.
        when(beerRepository.findAll()).thenReturn(Collections.EMPTY_LIST);

        //then
        List<BeerDTO> foundListBeersDTO = beerService.listAll();

        //Confira se a lista foundListaBeersDTO é vazia
        assertThat(foundListBeersDTO, is(empty()));
    }

    @Test
    void whenExclusionIsCalledWithValidIdThenABeerShouldBeDeleted() throws BeerNotFoundException {
        // given
        BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedDeletedBeer = beerMapper.toModel(expectedDeletedBeerDTO);

        // when
        /*
            Quando beerRepository chamar o método findById passando expectedDeleteBeerDTO.getid, então retorne uma Optional
            com o valor de expectedDeletedBeer
            Não faça nada, quando o beerRepository chamar o método deleteById;
         */
        when(beerRepository.findById(expectedDeletedBeerDTO.getId())).thenReturn(Optional.of(expectedDeletedBeer));
        doNothing().when(beerRepository).deleteById(expectedDeletedBeerDTO.getId());

        // then
        beerService.deleteById(expectedDeletedBeerDTO.getId());

        verify(beerRepository, times(1)).findById(expectedDeletedBeerDTO.getId());
        verify(beerRepository, times(1)).deleteById(expectedDeletedBeerDTO.getId());
    }

    //
    @Test
    void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
        //given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //when
        //Quando beerRepository chamar o método o findById, então retorne um Optional expectedBeer
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        //Quando beerRepository chamar o método salvar, retorne expectedBeer
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToIncrement = 10;
        int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;

        // then
        //Salvando em uma variável o valor do incremento do valor da quantity do objeto mais o valor da variável quantityToIncrement
        BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);

        //Confira se a expectedQuantityAfterIncrement é igual ao novo valor do objeto incrementedBeerDTO
        assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
        //Confira se o valor de expectedQuantityAfterIncrement é menor que o valor máximo do expectedBeerDTO
        assertThat(expectedQuantityAfterIncrement, lessThan(expectedBeerDTO.getMax()));
    }

    @Test
    void whenIncrementIsGreaterThanMaxThenThrowException() {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //Quando beerRepository chamar o método findById com expectedBeerDTO.getId como parâmetro, então retorne um Optional expectedBeer
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToIncrement = 80;

        //Confira se foi jogado uma exceção BeerStockExceeded, quando o beerService tentar incrementar um valor superior que seu max

        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenIncrementAfterSumIsGreaterThanMaxThenThrowException() {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToIncrement = 45;

        //Confira se foi jogado uma exceção BeerStockExceeded, quando beerService tentar somar um valor que vai ultrapassar o seu max.
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenIncrementIsCalledWithInvalidIdThenThrowException() {
        int quantityToIncrement = 10;

        //Retorna um Optional vazio quando o método findById for chamado
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        //Como o retorno será vazio, devemos esperar um erro que a cerveja não foi encontrada(BeerNotFoundException)
        assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, quantityToIncrement));
    }


    @Test
    void whenDecrementIsCalledThenDecrementBeerStock() throws BeerNotFoundException, BeerStockBelowZeroException {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        /*Quando meu beerRepository chamar o método findById passando expectedBeerDTo.getID, então retorne um Optional
        com o valor sendo expectedBeer*/
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        //Quando beerRepository chamar o método save passando expectedBeer, então retorne expectedBeer
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToDecrement = 5;
        int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;
        BeerDTO DecrementBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);

        //Confira se expectedQuantityAfterDecrement é igual ao decrementBeerDTO
        assertThat(expectedQuantityAfterDecrement, equalTo(DecrementBeerDTO.getQuantity()));
        //Confira se o valor do stock é maior que 0
        assertThat(expectedQuantityAfterDecrement, greaterThan(0));
   }

    @Test
    void whenDecrementIsCalledToEmptyStockThenEmptyBeerStock() throws BeerNotFoundException, BeerStockBelowZeroException {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToDecrement = 10;
        int expectedQuantityAfterDecrement = expectedBeerDTO.getQuantity() - quantityToDecrement;
        BeerDTO DecrementBeerDTO = beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement);

        /*Confira se expectedQuantityAfterDecrement é igual a zero, se for confira se expectedQuantityAfterDecrement é
        igual ao DecrementBeerDTO.getQuantity*/
        assertThat(expectedQuantityAfterDecrement, equalTo(0));
        assertThat(expectedQuantityAfterDecrement, equalTo(DecrementBeerDTO.getQuantity()));
    }

    @Test
    void whenDecrementIsLowerThanZeroThenThrowException() {
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToDecrement = 80;
        //Confira se é jogada uma exceção caso tentem tirar uma quantidade superior que existe no stock
        assertThrows(BeerStockBelowZeroException.class, () -> beerService.decrement(expectedBeerDTO.getId(), quantityToDecrement));
    }

    @Test
    void whenDecrementIsCalledWithInvalidIdThenThrowException() {
        int quantityToDecrement = 10;

        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());


        //Confira se é jogada uma exceção caso não encontre a cerveja.
        assertThrows(BeerNotFoundException.class, () -> beerService.decrement(INVALID_BEER_ID, quantityToDecrement));
    }

}