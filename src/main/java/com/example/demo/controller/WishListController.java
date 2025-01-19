package com.example.demo.controller;

import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.WishListMapper;
import com.example.demo.requestdto.WishListRequest;
import com.example.demo.responsedto.WishListResponse;
import com.example.demo.service.WishListService;
import com.example.demo.util.ResponseStructure;
import com.example.demo.util.Roles;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(allowedHeaders = "*",origins = "*")
@RestController
@RequestMapping("/wishlist")
@AllArgsConstructor
public class WishListController
{
    private WishListService wishListService;
    private UserMapper userMapper;
    private final WishListMapper wishListMapper=new WishListMapper();
    private static final String HEADER="Authorization";

    @PostMapping("/addToWishList")
    public ResponseEntity<ResponseStructure<WishListResponse>> addToWishList(
            @RequestHeader(value = HEADER)String authHeader,
            @Valid @RequestBody WishListRequest wishListRequest)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return wishListMapper.headerError();
        }
        return wishListService.addToWishList(userDetails.getUsername(), wishListRequest);
    }


    @GetMapping("/getWishList")
    public ResponseEntity<ResponseStructure<List<WishListResponse>>> getWishList(@RequestHeader(value = HEADER)String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return wishListMapper.noAuthority();
        }
        return wishListService.getWishList(userDetails.getUsername());
    }
}
