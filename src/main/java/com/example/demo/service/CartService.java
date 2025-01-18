package com.example.demo.service;

import com.example.demo.requestdto.CartRequestDto;
import com.example.demo.responsedto.CartResponseDto;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CartService
{
    ResponseEntity<ResponseStructure<CartResponseDto>> addToCart(String email, CartRequestDto cartRequestDto);

    ResponseEntity<ResponseStructure<CartResponseDto>> removeFromCart(String email, Long cartId);

    ResponseEntity<ResponseStructure<List<CartResponseDto>>> getCartItems(String email);

    ResponseEntity<ResponseStructure<CartResponseDto>> clearCart(String username);
}
