package com.example.bookstore.integrationtest;

import com.example.bookstore.entity.Book;
import com.example.bookstore.integrationtest.h2repo.BookH2Repository;
import com.example.bookstore.integrationtest.h2repo.UserH2Repository;
import com.example.bookstore.requestdto.BookRequest;
import com.example.bookstore.requestdto.UserLoginEntity;
import com.example.bookstore.requestdto.UserRegisterEntity;
import com.example.bookstore.responsedto.BookResponse;
import com.example.bookstore.responsedto.LoginResponse;
import com.example.bookstore.responsedto.RegisterResponse;
import com.example.bookstore.util.ResponseStructure;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class BookControllerITTests
{
    @LocalServerPort
    private int port;

    private String baseUrl="http://localhost";

    private static RestTemplate restTemplate;

    @Autowired
    private BookH2Repository bookH2Repository;

    @Autowired
    private UserH2Repository userH2Repository;

    @BeforeAll
    public static void init()
    {
        restTemplate=new RestTemplate();
    }

    @BeforeEach
    public void setUp()
    {
        baseUrl=baseUrl.concat(":").concat(port+"").concat("/book");
        bookH2Repository.deleteAll();
        userH2Repository.deleteAll();
    }

    @AfterEach
    public void tearDown()
    {
        bookH2Repository.deleteAll();
        userH2Repository.deleteAll();
    }

    protected String getAuthToken()
    {
        UserRegisterEntity userRegisterEntity = UserRegisterEntity.builder()
                .firstName("Test")
                .lastName("Chandu")
                .dob(LocalDate.of(2002,8,24))
                .email("test@gmail.com")
                .role("ADMIN")
                .password("Saichandu@090").build();

        ResponseEntity<ResponseStructure<RegisterResponse>> registerResponse = restTemplate.exchange( "http://localhost:"+port+"/register", HttpMethod.POST, new HttpEntity<>(userRegisterEntity), new ParameterizedTypeReference<ResponseStructure<RegisterResponse>>(){});

        assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),registerResponse.getBody().getStatus());

        UserLoginEntity userLoginEntity = UserLoginEntity.builder()
                .email("test@gmail.com")
                .password("Saichandu@090").build();

        ResponseEntity<ResponseStructure<LoginResponse>> loginResponse = restTemplate.exchange(  "http://localhost:"+port+"/login", HttpMethod.POST, new HttpEntity<>(userLoginEntity), new ParameterizedTypeReference<ResponseStructure<LoginResponse>>(){});

        assertEquals(HttpStatus.OK,loginResponse.getStatusCode());
        assertEquals(HttpStatus.OK.value(),loginResponse.getBody().getStatus());
        assertEquals("test@gmail.com",loginResponse.getBody().getData().getEmail());
        assertEquals("ADMIN",loginResponse.getBody().getData().getRole());
        return loginResponse.getBody().getMessage();
    }


    @Test
    void addBookTestValidScene()
    {
        String authToken=getAuthToken();

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        BookRequest bookRequest = BookRequest.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();

        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);

        ResponseEntity<ResponseStructure<BookResponse>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),response.getBody().getStatus());
        assertEquals(bookRequest.getBookName(),response.getBody().getData().getBookName());
    }

    @Test
    void addBookTestIfBodyIsInvalid()
    {
        String authToken=getAuthToken();

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        BookRequest bookRequest = BookRequest.builder()
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();

        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()-> restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {}));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }


    @Test
    void addBookTestIfBookQuantityIsLessThan16()
    {
        String authToken=getAuthToken();

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        BookRequest bookRequest = BookRequest.builder()
                .bookName("Something")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(11)
                .bookLogo("URL").build();

        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()-> restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {}));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode(),"If ADMIN tries to add a book with less than 16 quantity");
    }

    @Test
    void addBookTestIfTokenIsInvalidOrMissing()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","authToken");

        BookRequest bookRequest = BookRequest.builder()
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();

        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }


    @Test
    void getBookByNameValidScene()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        BookRequest bookRequest = BookRequest.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();
        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);
        ResponseEntity<ResponseStructure<BookResponse>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});
        assertEquals(HttpStatus.CREATED,response.getStatusCode());

        HttpEntity<Object> getEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<BookResponse>> getResponse = restTemplate.exchange(baseUrl + "/getBookByName/TEST", HttpMethod.GET, getEntity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});

        assertEquals(HttpStatus.OK,getResponse.getStatusCode());
        assertEquals(HttpStatus.OK.value(),getResponse.getBody().getStatus());
        assertEquals("TEST",getResponse.getBody().getData().getBookName());
        assertEquals(1,bookH2Repository.findAll().size());
    }

    @Test
    void getBookByNameIfWrongBookNameGiven()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        //adding a book
        BookRequest bookRequest = BookRequest.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();
        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);
        ResponseEntity<ResponseStructure<BookResponse>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});
        assertEquals(HttpStatus.CREATED,response.getStatusCode());

        //This test
        HttpEntity<Object> getEntity = new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getBookByName/TESTING", HttpMethod.GET, getEntity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {}));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }


    @Test
    void getBookByIdValidScene()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        //adding a book
        BookRequest bookRequest = BookRequest.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();
        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);
        ResponseEntity<ResponseStructure<BookResponse>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});
        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        Long bookId=response.getBody().getData().getBookId();

        //This test
        HttpEntity<Object> getEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<BookResponse>> getResponse = restTemplate.exchange(baseUrl + "/getBookById/"+bookId, HttpMethod.GET, getEntity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});

        assertEquals(HttpStatus.OK,getResponse.getStatusCode());
        assertEquals(HttpStatus.OK.value(),getResponse.getBody().getStatus());
        assertEquals(bookId,getResponse.getBody().getData().getBookId());
        assertEquals("TEST",getResponse.getBody().getData().getBookName());
        assertEquals(1,bookH2Repository.findAll().size());
    }


    @Test
    void getBookByIdIfBookIdNotFound()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        //adding a book
        BookRequest bookRequest = BookRequest.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();
        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);
        ResponseEntity<ResponseStructure<BookResponse>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});
        assertEquals(HttpStatus.CREATED,response.getStatusCode());

        //This test
        HttpEntity<Object> getEntity = new HttpEntity<>(httpHeaders);
        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getBookById/32451", HttpMethod.GET, getEntity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {}));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }


    @Test
    void updateBookValidScene()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        //Adding a book
        BookRequest bookRequest = BookRequest.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();
        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);
        ResponseEntity<ResponseStructure<BookResponse>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});
        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        Long bookId=response.getBody().getData().getBookId();

        //Editing the book
        BookRequest bookEditRequest = BookRequest.builder()
                .bookName("TESTING")
                .bookPrice(399.3)
                .bookAuthor("Manual Test")
                .bookDescription("Atom Bom")
                .bookQuantity(145)
                .bookLogo("URL").build();

        HttpEntity<Object> entity2 = new HttpEntity<>(bookEditRequest,httpHeaders);
        ResponseEntity<ResponseStructure<BookResponse>> response2 = restTemplate.exchange(baseUrl + "/updateBook/"+bookId, HttpMethod.PUT, entity2,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});
        assertEquals(HttpStatus.OK,response2.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response2.getBody().getStatus());
        assertEquals(bookId,response2.getBody().getData().getBookId());
        assertEquals("TESTING",response2.getBody().getData().getBookName());
        assertEquals(399.3,response2.getBody().getData().getBookPrice());
        assertEquals("Manual Test",response2.getBody().getData().getBookAuthor());
    }


    @Test
    void updateBookIfBookIdIsInvalid()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        //Adding a book
        BookRequest bookRequest = BookRequest.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();
        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);
        ResponseEntity<ResponseStructure<BookResponse>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});
        assertEquals(HttpStatus.CREATED,response.getStatusCode());

        //Editing the book
        BookRequest bookEditRequest = BookRequest.builder()
                .bookName("TESTING")
                .bookPrice(399.3)
                .bookAuthor("Manual Test")
                .bookDescription("Atom Bom")
                .bookQuantity(145)
                .bookLogo("URL").build();

        HttpEntity<Object> entity2 = new HttpEntity<>(bookEditRequest,httpHeaders);
        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/updateBook/32455", HttpMethod.PUT, entity2,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {
                }));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }


    @Test
    void updateBookIfBookBodyIsNotValid()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        //Adding a book
        BookRequest bookRequest = BookRequest.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();
        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);
        ResponseEntity<ResponseStructure<BookResponse>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});
        assertEquals(HttpStatus.CREATED,response.getStatusCode());

        //Updating book
        BookRequest bookEditRequest = BookRequest.builder()
                .bookPrice(399.3)
                .bookAuthor("Manual Test")
                .bookDescription("Atom Bom")
                .bookQuantity(145)
                .bookLogo("URL").build();

        HttpEntity<Object> entity2 = new HttpEntity<>(bookEditRequest,httpHeaders);
        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/updateBook/3245", HttpMethod.PUT, entity2,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {
                }));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }


    @Test
    void updateBookIfTokenIsNotValid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","authToken");
        BookRequest bookRequest = BookRequest.builder()
                .bookName("Testing")
                .bookPrice(399.3)
                .bookAuthor("Manual Test")
                .bookDescription("Atom Bom")
                .bookQuantity(145)
                .bookLogo("URL").build();
        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> restTemplate.exchange(baseUrl + "/updateBook/3245", HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {
                }));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }


    @Test
    void deleteBookValidScene()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        //Adding a book
        BookRequest bookRequest = BookRequest.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();
        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);
        ResponseEntity<ResponseStructure<BookResponse>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});
        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        Long bookId=response.getBody().getData().getBookId();

        //deleting the book
        HttpEntity<Object> entity2 = new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<String>> response2 = restTemplate.exchange(baseUrl + "/deleteBook/"+bookId, HttpMethod.DELETE, entity2,
                new ParameterizedTypeReference<ResponseStructure<String>>() {});

        assertEquals(HttpStatus.OK,response2.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response2.getBody().getStatus());
        assertEquals(1,bookH2Repository.findAll().size());
    }


    @Test
    void deleteBookIfBookIdIsInvalid()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        //Adding a book
        BookRequest bookRequest = BookRequest.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();
        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);
        ResponseEntity<ResponseStructure<BookResponse>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});
        assertEquals(HttpStatus.CREATED,response.getStatusCode());

        //deleting the book
        HttpEntity<Object> entity2 = new HttpEntity<>(httpHeaders);
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(baseUrl + "/deleteBook/" + 1234, HttpMethod.DELETE, entity2,
                    new ParameterizedTypeReference<ResponseStructure<String>>() {});
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }


    @Test
    void deleteBookIfTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","authToken");
        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(baseUrl + "/deleteBook/" + 3245, HttpMethod.DELETE, entity,
                    new ParameterizedTypeReference<ResponseStructure<String>>() {});
        });
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }


    void addBookTestValidSceneSecondBook(String authToken)
    {

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        BookRequest bookRequest = BookRequest.builder()
                .bookName("REST")
                .bookPrice(899.3)
                .bookAuthor("Sai")
                .bookDescription("Atom")
                .bookQuantity(708)
                .bookLogo("URL").build();

        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);
        ResponseEntity<ResponseStructure<BookResponse>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),response.getBody().getStatus());
        assertEquals(bookRequest.getBookName(),response.getBody().getData().getBookName());
    }


    @Test
    void getAllBooksTest()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        ResponseEntity<ResponseStructure<List<BookResponse>>> ifEmpty = restTemplate.exchange(baseUrl + "/getBooks", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {});

        assertEquals(HttpStatus.NO_CONTENT,ifEmpty.getStatusCode());

        //Adding a book
        BookRequest bookRequest = BookRequest.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();
        HttpEntity<Object> entity = new HttpEntity<>(bookRequest,httpHeaders);
        ResponseEntity<ResponseStructure<BookResponse>> response7 = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponse>>() {});
        assertEquals(HttpStatus.CREATED,response7.getStatusCode());

        addBookTestValidSceneSecondBook(authToken);

        ResponseEntity<ResponseStructure<List<BookResponse>>> response = restTemplate.exchange(baseUrl + "/getBooks", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(2,bookH2Repository.findAll().size());
        assertEquals(2,response.getBody().getData().size());
    }

    @Test
    void getAllBooksTestIfTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","authToken");
        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getBooks", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }


    @Test
    void findBooksWithSortingBookNameBookPriceValidTest()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        Book first=Book.builder()
                .bookName("Lawsuit")
                .bookAuthor("Carley")
                .bookPrice(220.99)
                .status(true)
                .build();

        Book second=Book.builder()
                .bookName("Zak Crawly")
                .bookAuthor("Brother")
                .bookPrice(225.00)
                .status(true)
                .build();

        Book third=Book.builder()
                .bookName("James miggel")
                .bookAuthor("Chuck")
                .bookPrice(99.90)
                .status(true)
                .build();
        bookH2Repository.save(first);
        bookH2Repository.save(second);
        bookH2Repository.save(third);

        //Based on bookPrice
        ResponseEntity<ResponseStructure<List<BookResponse>>> response = restTemplate.exchange(baseUrl + "/sortBy/bookPrice", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(3,bookH2Repository.findAll().size());
        assertEquals(3,response.getBody().getData().size());
        assertEquals(99.90,response.getBody().getData().getFirst().getBookPrice());
        assertEquals(220.99,response.getBody().getData().get(1).getBookPrice());
        assertEquals(225.00,response.getBody().getData().get(2).getBookPrice());


        //Based on bookName
        ResponseEntity<ResponseStructure<List<BookResponse>>> response2 = restTemplate.exchange(baseUrl + "/sortBy/bookName", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {});

        assertEquals(HttpStatus.OK,response2.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response2.getBody().getStatus());
        assertEquals(3,bookH2Repository.findAll().size());
        assertEquals(3,response2.getBody().getData().size());
        assertEquals("James miggel",response2.getBody().getData().getFirst().getBookName());
        assertEquals("Lawsuit",response2.getBody().getData().get(1).getBookName());
        assertEquals("Zak Crawly",response2.getBody().getData().get(2).getBookName());


        //Based on bookAuthor
        ResponseEntity<ResponseStructure<List<BookResponse>>> response4 = restTemplate.exchange(baseUrl + "/sortBy/bookAuthor", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {});

        assertEquals(HttpStatus.OK,response4.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response4.getBody().getStatus());
        assertEquals(3,bookH2Repository.findAll().size());
        assertEquals(3,response4.getBody().getData().size());
        assertEquals("Brother",response4.getBody().getData().getFirst().getBookAuthor());
        assertEquals("Carley",response4.getBody().getData().get(1).getBookAuthor());
        assertEquals("Chuck",response4.getBody().getData().get(2).getBookAuthor());
    }


    @Test
    void sortByFieldWithInvalidField()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/sortBy/bookPric", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {}));
        assertEquals(HttpStatus.BAD_REQUEST,exception.getStatusCode());
    }


    @Test
    void paginationValidTest()
    {
        String authToken = getAuthToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + authToken);

        IntStream.range(1, 16).forEach(i -> {
            Book book = Book.builder()
                    .bookName("Book " + i)
                    .bookAuthor("Author " + i)
                    .bookPrice(1 + i * 10.7)
                    .status(true)
                    .build();
            bookH2Repository.save(book);
        });

        //Testing the default param values
        ResponseEntity<ResponseStructure<List<BookResponse>>> defaultResponse = restTemplate.exchange(baseUrl + "/pagination", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {});
        assertEquals(HttpStatus.OK, defaultResponse.getStatusCode());
        assertEquals(HttpStatus.OK.value(), defaultResponse.getBody().getStatus());
        assertEquals(8,defaultResponse.getBody().getData().size(),"Checking the number of books equals to page size");
        assertEquals(15, bookH2Repository.findAll().size());

        //First page Test
        ResponseEntity<ResponseStructure<List<BookResponse>>> response = restTemplate.exchange(baseUrl + "/pagination?pageSize=10&pageNumber=0", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals(10,response.getBody().getData().size(),"Checking the number of books equals to page size");


        //Second page Test
        ResponseEntity<ResponseStructure<List<BookResponse>>> response2 = restTemplate.exchange(baseUrl + "/pagination?pageSize=10&pageNumber=1", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {});
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(HttpStatus.OK.value(), response2.getBody().getStatus());
        assertEquals(5,response2.getBody().getData().size(),"Checking the number of books");


        //Third page Test
        ResponseEntity<ResponseStructure<List<BookResponse>>> response3 = restTemplate.exchange(baseUrl + "/pagination?pageSize=10&pageNumber=2", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {});
        assertEquals(HttpStatus.NO_CONTENT, response3.getStatusCode(),"No content should get displayed as page number and size exceeds the total books");
    }


    @Test
    void paginationIfPageNumberIsInvalidPageNumberExceedsBooksSize()
    {
        String authToken = getAuthToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + authToken);

        IntStream.range(1, 16).forEach(i -> {
            Book book = Book.builder()
                    .bookName("Book " + i)
                    .bookAuthor("Author " + i)
                    .bookPrice(1 + i * 10.7)
                    .build();
            bookH2Repository.save(book);
        });

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class, ()->restTemplate.exchange(baseUrl + "/pagination?pageSize=10&pageNumber=-1", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {}));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode(),"If page number is Invalid");


        ResponseEntity<ResponseStructure<List<BookResponse>>> response2 = restTemplate.exchange(baseUrl + "/pagination?pageSize=10&pageNumber=10", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {});
        assertEquals(HttpStatus.NO_CONTENT, response2.getStatusCode(),"If page number exceeds total books size");
    }


    @Test
    void searchQueryCheckingForMatches()
    {
        String authToken = getAuthToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer "+authToken);
        Book first = Book.builder()
                .bookName("Lawsuit")
                .bookDescription("laws")
                .bookAuthor("Carley")
                .bookPrice(220.99)
                .status(true)
                .build();

        Book second = Book.builder()
                .bookName("James Crawly")
                .bookDescription("study of laws")
                .bookAuthor("Brother")
                .bookPrice(225.00)
                .status(true)
                .build();

        Book third = Book.builder()
                .bookName("James miggel")
                .bookDescription("gaming")
                .bookAuthor("Chuck")
                .bookPrice(99.90)
                .status(true)
                .build();
        bookH2Repository.save(first);
        bookH2Repository.save(second);
        bookH2Repository.save(third);


        ResponseEntity<ResponseStructure<List<BookResponse>>> response = restTemplate.exchange(baseUrl + "/search/laws", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(HttpStatus.OK.value(), response.getBody().getStatus());
        assertEquals(2,response.getBody().getData().size(),"2 books should be fetched as 2 books contains laws in their description");


        ResponseEntity<ResponseStructure<List<BookResponse>>> response2 = restTemplate.exchange(baseUrl + "/search/James", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {});
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(HttpStatus.OK.value(), response2.getBody().getStatus());
        assertEquals(2,response2.getBody().getData().size(),"2 books should be fetched as we have 2 authors named James");


        ResponseEntity<ResponseStructure<List<BookResponse>>> response3 = restTemplate.exchange(baseUrl + "/search/Chuck", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {});
        assertEquals(HttpStatus.OK, response3.getStatusCode());
        assertEquals(HttpStatus.OK.value(), response3.getBody().getStatus());
        assertEquals(1,response3.getBody().getData().size(),"Query is looking case sensitive");


        ResponseEntity<ResponseStructure<List<BookResponse>>> response4 = restTemplate.exchange(baseUrl + "/search/business", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponse>>>() {});
        assertEquals(HttpStatus.NO_CONTENT, response4.getStatusCode(),"If nothing matches");
    }
}
