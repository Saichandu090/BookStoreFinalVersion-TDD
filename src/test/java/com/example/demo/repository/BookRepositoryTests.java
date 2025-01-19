package com.example.demo.repository;

import com.example.demo.entity.Book;
import com.example.demo.exception.BookNotFoundException;
import com.example.demo.requestdto.BookRequest;
import com.example.demo.responsedto.BookResponse;
import com.example.demo.service.BookService;
import com.example.demo.util.ResponseStructure;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BookRepositoryTests
{
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @Test
    public void addBookTest()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookId(789654L)
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        ResponseEntity<ResponseStructure<BookResponse>> response=bookService.addBook(bookRequest);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookRequest.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Optional<Book> book=bookRepository.findById(bookRequest.getBookId());
        assertNotNull(book,"Book should not be null");

        Book actual=book.get();
        assertEquals(bookRequest.getBookId(),actual.getBookId());
        assertEquals(bookRequest.getBookName(),actual.getBookName());
    }


    @Test
    public void getBookByIdTest()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookId(789654123L)
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookService.addBook(bookRequest);

        ResponseEntity<ResponseStructure<BookResponse>> response=bookService.getBookById(bookRequest.getBookId());

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookRequest.getBookId());
        Assertions.assertThat(response.getBody().getData().getBookName()).isEqualTo(bookRequest.getBookName());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void getBookByNameTest()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookId(789654123L)
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookService.addBook(bookRequest);

        ResponseEntity<ResponseStructure<BookResponse>> response=bookService.getBookByName(bookRequest.getBookName());

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookRequest.getBookId());
        Assertions.assertThat(response.getBody().getData().getBookName()).isEqualTo(bookRequest.getBookName());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void getAllBooksTest()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookId(789654123L)
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequest bookRequest2 = BookRequest.builder()
                .bookId(789654126L)
                .bookName("ZXCFG")
                .bookPrice(989.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookService.addBook(bookRequest);
        bookService.addBook(bookRequest2);

        ResponseEntity<ResponseStructure<List<BookResponse>>> response=bookService.getAllBooks();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(2);
        Assertions.assertThat(response.getBody().getData().getFirst().getBookName()).isEqualTo(bookRequest.getBookName());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        assertEquals(response.getBody().getData().get(1).getBookId(), bookRequest2.getBookId());
        assertEquals(response.getBody().getData().get(1).getBookName(), bookRequest2.getBookName());
    }

    @Test
    public void findBooksWithSortingBookName()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookId(789654123L)
                .bookName("Annabell")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequest bookRequest1 = BookRequest.builder()
                .bookId(789654121L)
                .bookName("Chill")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequest bookRequest2 = BookRequest.builder()
                .bookId(789654126L)
                .bookName("Zing")
                .bookPrice(989.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookService.addBook(bookRequest1);
        bookService.addBook(bookRequest);
        bookService.addBook(bookRequest2);

        ResponseEntity<ResponseStructure<List<BookResponse>>> response=bookService.findBooksWithSorting("bookName");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(3);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        assertEquals(response.getBody().getData().get(1).getBookId(), bookRequest1.getBookId());
        assertEquals(response.getBody().getData().get(1).getBookName(), bookRequest1.getBookName());

        assertEquals(response.getBody().getData().get(2).getBookId(), bookRequest2.getBookId());
        assertEquals(response.getBody().getData().get(2).getBookName(), bookRequest2.getBookName());

        assertEquals(response.getBody().getData().getFirst().getBookId(), bookRequest.getBookId());
        assertEquals(response.getBody().getData().getFirst().getBookName(), bookRequest.getBookName());
    }

    @Test
    public void findBooksWithSortingBookPrice()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookId(789654123L)
                .bookName("Annabell")
                .bookPrice(189.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequest bookRequest1 = BookRequest.builder()
                .bookId(789654121L)
                .bookName("Chill")
                .bookPrice(389.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequest bookRequest2 = BookRequest.builder()
                .bookId(789654126L)
                .bookName("Zing")
                .bookPrice(989.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookService.addBook(bookRequest1);
        bookService.addBook(bookRequest);
        bookService.addBook(bookRequest2);

        ResponseEntity<ResponseStructure<List<BookResponse>>> response=bookService.findBooksWithSorting("bookPrice");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(3);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        assertEquals(response.getBody().getData().get(1).getBookId(), bookRequest1.getBookId());
        assertEquals(response.getBody().getData().get(1).getBookName(), bookRequest1.getBookName());
        assertEquals(response.getBody().getData().get(1).getBookPrice(), bookRequest1.getBookPrice());

        assertEquals(response.getBody().getData().get(2).getBookId(), bookRequest2.getBookId());
        assertEquals(response.getBody().getData().get(2).getBookName(), bookRequest2.getBookName());
        assertEquals(response.getBody().getData().get(2).getBookPrice(), bookRequest2.getBookPrice());

        assertEquals(response.getBody().getData().getFirst().getBookId(), bookRequest.getBookId());
        assertEquals(response.getBody().getData().getFirst().getBookName(), bookRequest.getBookName());
        assertEquals(response.getBody().getData().getFirst().getBookPrice(), bookRequest.getBookPrice());
    }


    @Test
    public void updateBookTest()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookId(789654123L)
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        ResponseEntity<ResponseStructure<BookResponse>> response=bookService.addBook(bookRequest);

        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookRequest.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Optional<Book> book=bookRepository.findById(bookRequest.getBookId());
        assertNotNull(book,"Book should not be null");

        Book actual=book.get();
        assertEquals(bookRequest.getBookId(),actual.getBookId());
        assertEquals(bookRequest.getBookName(),actual.getBookName());

        BookRequest updatableBook= BookRequest.builder()
                .bookName("ZXC")
                .bookPrice(489.0)
                .bookLogo("URI")
                .bookAuthor("XYZMJ")
                .bookDescription("Description")
                .bookQuantity(858).build();

        ResponseEntity<ResponseStructure<BookResponse>> findBook=bookService.updateBook(bookRequest.getBookId(),updatableBook);

        Assertions.assertThat(findBook.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(findBook.getBody().getData().getBookId()).isEqualTo(bookRequest.getBookId());
        Assertions.assertThat(findBook.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        BookResponse result=findBook.getBody().getData();

        assertEquals(result.getBookId(), bookRequest.getBookId());
        assertEquals(result.getBookName(),updatableBook.getBookName());
    }

    @Test
    public void deleteBookTest()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookId(789654123L)
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        ResponseEntity<ResponseStructure<BookResponse>> response=bookService.addBook(bookRequest);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookRequest.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Optional<Book> book=bookRepository.findById(bookRequest.getBookId());
        assertNotNull(book,"Book should not be null");

        Book actual=book.get();
        assertEquals(bookRequest.getBookId(),actual.getBookId());
        assertEquals(bookRequest.getBookName(),actual.getBookName());

        ResponseEntity<ResponseStructure<String>> deleteResponse=bookService.deleteBook(bookRequest.getBookId());

        Assertions.assertThat(deleteResponse.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(deleteResponse.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        assertThrows(BookNotFoundException.class,()->bookService.deleteBook(bookRequest.getBookId()));

        assertThrows(BookNotFoundException.class,()->bookService.getBookById(bookRequest.getBookId()));
    }


    @Test
    public void bookRepositoryFindByBookNameTestMustReturnBook()
    {
        Book book= Book.builder()
                .bookId(1L)
                .bookName("Atom")
                .bookPrice(199.9)
                .bookAuthor("James")
                .bookDescription("Self")
                .bookQuantity(78)
                .bookLogo("Url").build();

        Book savedBook=bookRepository.save(book);

        Assertions.assertThat(savedBook.getBookId()).isEqualTo(book.getBookId());

        Book findBook=bookRepository.findByBookName(book.getBookName()).orElseThrow(()->new BookNotFoundException("Book not Found"));
        Assertions.assertThat(findBook.getBookId()).isEqualTo(book.getBookId());
        Assertions.assertThat(findBook.getBookName()).isEqualTo(book.getBookName());
    }


    @Test
    public void bookRepositoryFindByBookNameTestMustThrowBookNotFoundException()
    {
        Book book= Book.builder()
                .bookId(1L)
                .bookName("Atom")
                .bookPrice(199.9)
                .bookAuthor("James")
                .bookDescription("Self")
                .bookQuantity(78)
                .bookLogo("Url").build();

        Book savedBook=bookRepository.save(book);

        Assertions.assertThat(savedBook.getBookId()).isEqualTo(book.getBookId());

        org.junit.jupiter.api.Assertions.assertThrows(BookNotFoundException.class,()->bookRepository.findByBookName("Random").orElseThrow(()->new BookNotFoundException("Book not Found")));
    }
}
