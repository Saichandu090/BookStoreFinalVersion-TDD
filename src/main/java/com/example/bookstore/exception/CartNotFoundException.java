package com.example.bookstore.exception;

public class CartNotFoundException extends RuntimeException
{
    public CartNotFoundException(String message)
    {
        super(message);
    }
}
